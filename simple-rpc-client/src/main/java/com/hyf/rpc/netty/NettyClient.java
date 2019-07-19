package com.hyf.rpc.netty;

import com.hyf.rpc.netty.config.NettyConfig;
import com.hyf.rpc.netty.handler.PacketCodecHandler;
import com.hyf.rpc.netty.handler.RPCResponsePacketHandler;
import com.hyf.rpc.netty.handler.Spliter;
import com.hyf.rpc.netty.packet.Packet;
import io.netty.bootstrap.Bootstrap;
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
        // RPC调用
        NioEventLoopGroup workGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        RPCResponsePacketHandler responsePacketHandler = new RPCResponsePacketHandler();
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
                            ch.pipeline().addLast(responsePacketHandler);
                        }
                    });
            // 同步等待连接成功
            ChannelFuture future = bootstrap.connect(config.getIp(),config.getPort()).sync();
            if (future.isSuccess()){
                // 同步等待发送成功
                future.channel().writeAndFlush(packet).sync();
            }
            //同步等待RPCClientHandler的channelRead被触发后（意味着收到了调用结果）
            future.channel().closeFuture().sync();
        }catch (Exception e){
            return "调用失败";
        }finally {
            workGroup.shutdownGracefully();
        }
        return responsePacketHandler.getResult();
    }
}
