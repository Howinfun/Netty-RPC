package com.hyf.rpc.netty.common;

import com.hyf.rpc.netty.packet.Packet;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/22
 */
@AllArgsConstructor
@Data
public class NettyRpcTask implements Runnable{
    private Channel channel;
    private Packet requestPacket;
    @Override
    public void run() {
        channel.writeAndFlush(requestPacket);
    }
}
