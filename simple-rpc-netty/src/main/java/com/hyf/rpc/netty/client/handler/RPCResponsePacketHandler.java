package com.hyf.rpc.netty.client.handler;

import com.hyf.rpc.netty.attributes.Attributes;
import com.hyf.rpc.netty.common.ChannelPool;
import com.hyf.rpc.netty.packet.RPCResponsePacket;
import com.hyf.rpc.zookeeper.config.ZookeeperCache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Data
@Slf4j
public class RPCResponsePacketHandler extends SimpleChannelInboundHandler<RPCResponsePacket> {

    private Object result;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponsePacket rpcResponsePacket) throws Exception {
        this.result = rpcResponsePacket.getResult();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.error("服务端关闭");
            // 如果出现异常，则从ChannelPool移除通道并且关闭体通道
            String server = ctx.channel().attr(Attributes.IP_PORT).get();
            ChannelPool.removeChannel(server);
            // 服务列表删去服务端IP
            ZookeeperCache.removeIPPojo(server);
            ctx.channel().close();
            log.error("关闭缓存通道");
        }else {
            super.exceptionCaught(ctx,cause);
        }
    }
}
