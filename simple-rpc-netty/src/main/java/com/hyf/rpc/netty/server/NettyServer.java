package com.hyf.rpc.netty.server;

import com.hyf.rpc.netty.handler.PacketCodecHandler;
import com.hyf.rpc.netty.handler.Spliter;
import com.hyf.rpc.netty.properties.NettyProperties;
import com.hyf.rpc.netty.server.handler.RPCRequestPacketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/16
 */
@Component
public class NettyServer {
    @Autowired
    private NettyProperties nettyProperties;

    private ServerBootstrap bootstrap;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workGroup;

    private NettyServer(){
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new Spliter());
                        ch.pipeline().addLast(PacketCodecHandler.INSTANCE);
                        ch.pipeline().addLast(RPCRequestPacketHandler.INSTANCE);
                    }
                });
    }

    public void start(){
        try {
            ChannelFuture future = bootstrap.bind(nettyProperties.getServerPort()).sync();
            if (future.isSuccess()){
                System.out.println("netty服务启动成功，端口号为："+nettyProperties.getServerPort());
            }
        } catch (InterruptedException e) {
            System.err.println("netty服务启动失败");
        }
    }

    /**
     * 优雅的关闭
     */
    @PreDestroy
    public void shutDown(){
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
