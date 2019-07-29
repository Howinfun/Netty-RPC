package com.hyf.rpc.netty.client;

import com.hyf.rpc.netty.attributes.Attributes;
import com.hyf.rpc.netty.client.handler.RPCResponsePacketHandler;
import com.hyf.rpc.netty.common.ChannelPool;
import com.hyf.rpc.netty.common.Result;
import com.hyf.rpc.netty.handler.PacketCodecHandler;
import com.hyf.rpc.netty.handler.Spliter;
import com.hyf.rpc.netty.packet.RPCRequestPacket;
import com.hyf.rpc.netty.properties.NettyProperties;
import com.hyf.rpc.netty.utils.IpUtil;
import com.hyf.rpc.zookeeper.config.ZookeeperCache;
import com.hyf.rpc.zookeeper.pojo.IPPojo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Component
@Slf4j
public class NettyClient {

    @Autowired
    private NettyProperties nettyProperties;
    private static NettyProperties properties;
    @PostConstruct
    public void init(){
        properties = this.nettyProperties;
    }

    public static Result callRPC(RPCRequestPacket packet) throws Exception{
        Result result = new Result();
        // 根据Packet的服务看有哪些应用提供服务，优先调用有缓存channel的，否则就创建channel并缓存起来
        String interfaceName = packet.getClazz().getName();
        String version = packet.getVersion();
        Set<IPPojo> ips = ZookeeperCache.getServiceList().get(interfaceName+version);
        if (ips.size() <=0 ){
            result.setSuccess(false);
            result.setMsg("无服务提供");
            return result;
        }
        List<String> ipList = ips.stream().map(ipPojo -> ipPojo.getIp()+":"+ipPojo.getPort()).collect(Collectors.toList());
        Channel channel = ChannelPool.getChannelByContainKey(ipList);
        // 如果有缓存的channel，直接获取channel进行rpc通信
        if (channel != null){
            try {
                log.info("走缓存拿Channel来通信");
                channel.writeAndFlush(packet);
                // 死循环等待服务端响应结果
                while (true){
                    RPCResponsePacketHandler responsePacketHandler = (RPCResponsePacketHandler)channel.pipeline().get(IpUtil.getIp()+"ResponseHandler");
                    if (responsePacketHandler.getResult() != null){
                        result.setResult(responsePacketHandler.getResult());
                        return result;
                    }
                }
            }catch (Exception e){
                // 如果出现异常，则从ChannelPool移除通道并且关闭体通道
                String server = channel.attr(Attributes.IP_PORT).get();
                ChannelPool.removeChannel(server);
                // 服务列表删去服务端IP
                ZookeeperCache.removeIPPojo(server);
                channel.close();
                log.error("关闭缓存通道");
                // 重新遍历其他ip，尝试继续创建新的Channel通信
                ips = ZookeeperCache.getServiceList().get(interfaceName+version);
                if (ips.size() <=0 ){
                    result.setSuccess(false);
                    result.setMsg("无服务提供");
                    return result;
                }
                ipList = ips.stream().map(ipPojo -> ipPojo.getIp()+":"+ipPojo.getPort()).collect(Collectors.toList());
                result = createNewChannel(packet,ipList);
                return result;
            }
        // 如果没有缓存的channel，就循环遍历ip来进行rpc通信，连接成功还需缓存channel
        }else{
            result = createNewChannel(packet,ipList);
            return result;
        }
    }

    private static Result createNewChannel(RPCRequestPacket packet, List<String> ipList){
        Result result = new Result();
        log.info("创建新的Channel来通信");
        for (String ip : ipList) {
            // RPC调用
            NioEventLoopGroup workGroup = new NioEventLoopGroup(1);
            Bootstrap bootstrap = new Bootstrap();
            try{
                bootstrap.group(workGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.TCP_NODELAY,true)
                        // 给通道添加属性，来标识是连接哪个服务端的
                        .attr(Attributes.IP_PORT,ip)
                        .handler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new Spliter());
                                ch.pipeline().addLast(PacketCodecHandler.INSTANCE);
                                ch.pipeline().addLast(IpUtil.getIp()+"ResponseHandler",new RPCResponsePacketHandler());
                            }
                        });
                // 同步等待连接成功
                ChannelFuture future = bootstrap.connect(properties.getClientIp(),properties.getClientPort()).sync();
                if (future.isSuccess()){
                    // 同步等待发送成功
                    future.channel().writeAndFlush(packet).sync();
                    // 将Channel放入缓存中
                    ChannelPool.addChannel(ip,future.channel());
                    // 死循环等待服务端响应结果
                    while (true){
                        RPCResponsePacketHandler responsePacketHandler = (RPCResponsePacketHandler)future.channel().pipeline().get(IpUtil.getIp()+"ResponseHandler");
                        if (responsePacketHandler.getResult() != null){
                            result.setResult(responsePacketHandler.getResult());
                            responsePacketHandler.setResult(null);
                            return result;
                        }
                    }
                }else{
                    continue;
                }
            }catch (Exception e){
                // 报异常还是继续遍历ip列表进行RPC通信
                log.error("连接"+ip+"进行RPC通信失败");
                continue;
            }
        }
        result.setSuccess(false);
        result.setMsg("调用失败");
        return result;
    }
}
