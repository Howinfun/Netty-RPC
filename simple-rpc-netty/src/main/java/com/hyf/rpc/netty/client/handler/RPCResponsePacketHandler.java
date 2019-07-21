package com.hyf.rpc.netty.client.handler;

import com.hyf.rpc.netty.packet.RPCResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Data
public class RPCResponsePacketHandler extends SimpleChannelInboundHandler<RPCResponsePacket> {

    private Object result;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponsePacket rpcResponsePacket) throws Exception {
        this.result = rpcResponsePacket.getResult();
        // 接受到结果后记得关闭通道，不然InvocationHandler不能获取到结果
        ctx.channel().close();
    }
}
