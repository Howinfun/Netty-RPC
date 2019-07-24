package com.hyf.rpc.netty.client;

import com.hyf.rpc.netty.client.handler.RPCResponsePacketHandler;
import com.hyf.rpc.netty.common.ChannelPool;
import com.hyf.rpc.netty.config.NettyConfig;
import com.hyf.rpc.netty.handler.PacketCodecHandler;
import com.hyf.rpc.netty.handler.Spliter;
import com.hyf.rpc.netty.packet.Packet;
import com.hyf.rpc.netty.utils.IpUtil;
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

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Component
public class NettyClient {

    @Autowired
    private NettyConfig nettyConfig;
    private static NettyConfig config;
    @PostConstruct
    public void init(){
        config = this.nettyConfig;
    }

    public static Object callRPC(Packet packet) throws Exception{
        Object result;
        // 判断缓冲中是否有Channel
        String clientId = IpUtil.getIp()+"Client";
        if (ChannelPool.containChannel(clientId)){
            Channel channel = ChannelPool.getChannel(clientId);
            channel.writeAndFlush(packet);
            // 死循环等待服务端响应结果
            while (true){
                RPCResponsePacketHandler responsePacketHandler = (RPCResponsePacketHandler)channel.pipeline().get(IpUtil.getIp()+"ResponseHandler");
                if (responsePacketHandler.getResult() != null){
                    result =  responsePacketHandler.getResult();
                    return result;
                }
            }
        }else{
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
                ChannelFuture future = bootstrap.connect(config.getClientIp(),config.getClientPort()).sync();
                if (future.isSuccess()){
                    // 同步等待发送成功
                    future.channel().writeAndFlush(packet).sync();
                    ChannelPool.addChannel(clientId,future.channel());
                }
                // 死循环等待服务端响应结果
                while (true){
                    RPCResponsePacketHandler responsePacketHandler = (RPCResponsePacketHandler)future.channel().pipeline().get(IpUtil.getIp()+"ResponseHandler");
                    if (responsePacketHandler.getResult() != null){
                        result =  responsePacketHandler.getResult();
                        responsePacketHandler.setResult(null);
                        return result;
                    }
                }
            }catch (Exception e){
                return "调用失败";
            }
        }
    }
}
