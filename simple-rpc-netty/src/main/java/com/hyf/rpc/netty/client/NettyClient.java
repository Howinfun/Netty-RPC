package com.hyf.rpc.netty.client;

import cn.hutool.core.util.StrUtil;
import com.hyf.rpc.netty.client.handler.RPCResponsePacketHandler;
import com.hyf.rpc.netty.common.ChannelPool;
import com.hyf.rpc.netty.handler.PacketCodecHandler;
import com.hyf.rpc.netty.handler.Spliter;
import com.hyf.rpc.netty.packet.RPCRequestPacket;
import com.hyf.rpc.netty.properties.NettyProperties;
import com.hyf.rpc.netty.utils.IpUtil;
import com.hyf.rpc.zookeeper.config.ZookeeperListener;
import com.hyf.rpc.zookeeper.pojo.IPPojo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
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
public class NettyClient {

    @Autowired
    private NettyProperties nettyProperties;
    private static NettyProperties properties;
    @PostConstruct
    public void init(){
        properties = this.nettyProperties;
    }

    public static Object callRPC(RPCRequestPacket packet) throws Exception{
        Object result;
        // 根据Packet的服务看有哪些应用提供服务，优先调用有缓存channel的，否则就创建channel并缓存起来
        String interfaceName = packet.getClazz().getName();
        String version = packet.getVersion();
        Set<IPPojo> ips = ZookeeperListener.serviceList.get(interfaceName+version);
        if (ips.size() <=0 ){
            return "无服务提供";
        }
        List<String> ipList = ips.stream().map(ipPojo -> ipPojo.getIp()+":"+ipPojo.getPort()).collect(Collectors.toList());
        String containIP = ChannelPool.getContainKey(ipList);
        // 如果有缓存的channel，直接获取channel进行rpc通信
        if (!StrUtil.isBlank(containIP)){
            Channel channel = ChannelPool.getChannel(containIP);
            channel.writeAndFlush(packet);
            // 死循环等待服务端响应结果
            while (true){
                RPCResponsePacketHandler responsePacketHandler = (RPCResponsePacketHandler)channel.pipeline().get(IpUtil.getIp()+"ResponseHandler");
                if (responsePacketHandler.getResult() != null){
                    result =  responsePacketHandler.getResult();
                    return result;
                }
            }
        // 如果没有缓存的channel，就循环遍历ip来进行rpc通信，连接成功还需缓存channel
        }else{
            for (String ip : ipList) {
                // RPC调用
                NioEventLoopGroup workGroup = new NioEventLoopGroup(1);
                Bootstrap bootstrap = new Bootstrap();
                try{
                    bootstrap.group(workGroup)
                            .channel(NioSocketChannel.class)
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.TCP_NODELAY,true)
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
                        ChannelPool.addChannel(ip,future.channel());
                        // 死循环等待服务端响应结果
                        while (true){
                            RPCResponsePacketHandler responsePacketHandler = (RPCResponsePacketHandler)future.channel().pipeline().get(IpUtil.getIp()+"ResponseHandler");
                            if (responsePacketHandler.getResult() != null){
                                result =  responsePacketHandler.getResult();
                                responsePacketHandler.setResult(null);
                                return result;
                            }
                        }
                    }else{
                        continue;
                    }
                }catch (Exception e){
                    return "调用失败";
                }
            }
        }
        return "";
    }
}
