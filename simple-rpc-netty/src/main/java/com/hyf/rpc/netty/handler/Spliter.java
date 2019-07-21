package com.hyf.rpc.netty.handler;

import com.hyf.rpc.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
public class Spliter extends LengthFieldBasedFrameDecoder {

    private static final Integer LENGTH_FIELD_OFFSET = 7;
    private static final Integer LENGTH_FIELD_LENGTH = 4;

    public Spliter() {
        super(Integer.MAX_VALUE, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        /**
         * 请注意，使用get来获取魔数，因为set/get不影响读写指针的位置，不然如果使用readInt的话，要重新设置好readIndex
         * 如果魔数不对，则关闭通道并返回null
         */
        if (in.getInt(in.readerIndex())!= Packet.MAGIC_NUMBER){
            ctx.channel().close();
            return null;
        }
        return super.decode(ctx, in);
    }
}
