package com.hyf.rpc.netty.utils;

import com.hyf.rpc.netty.command.Command;
import com.hyf.rpc.netty.packet.Packet;
import com.hyf.rpc.netty.packet.RPCRequestPacket;
import com.hyf.rpc.netty.packet.RPCResponsePacket;
import com.hyf.rpc.netty.serialize.Serializer;
import com.hyf.rpc.netty.serialize.SerializerAlogrithm;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
public class PacketCodec {

    /** 序列化算法 */
    private static final Map<Byte, Serializer> serializerMap = new HashMap<>(1);
    /** packet */
    private static final Map<Byte,Class<? extends Packet>> packetMap = new HashMap<>(2);

    /** 单例 */
    public static final PacketCodec INSTANCE = new PacketCodec();

    private PacketCodec(){
        serializerMap.put(SerializerAlogrithm.JSON,Serializer.DEFAULT);
        packetMap.put(Command.RPC_REQUEST, RPCRequestPacket.class);
        packetMap.put(Command.RPC_RESPONSE, RPCResponsePacket.class);
    }

    public ByteBuf encode(ChannelHandlerContext ctx,Packet packet){
        ByteBuf byteBuf = ctx.alloc().ioBuffer();
        /** 魔数 4字节 */
        byteBuf.writeInt(Packet.MAGIC_NUMBER);
        /** 版本号 1字节 */
        byteBuf.writeByte(Packet.VERSION);
        /** 序列化算法 1字节 */
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlogrithm());
        /** 指令 1字节 */
        byteBuf.writeByte(packet.getCommand());
        /** 数据长度 4字节 */
        byte[] data = Serializer.DEFAULT.serialize(packet);
        byteBuf.writeInt(data.length);
        /** 数据 n字节 */
        byteBuf.writeBytes(data);
        return byteBuf;
    }

    public Packet decode(ByteBuf byteBuf){
        /** 跳过魔数 */
        byteBuf.skipBytes(4);
        /** 跳过版本号 */
        byteBuf.skipBytes(1);
        /** 获取序列化算法 */
        Byte serializerAlogrithm = byteBuf.readByte();
        Serializer serializer = serializerMap.get(serializerAlogrithm);
        /** 指令 */
        Byte command = byteBuf.readByte();
        /** 数据长度 */
        int dataLength = byteBuf.readInt();
        /** 获取数据 */
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        /** 反序列化 */
        Packet packet = serializer.deSerialize(data,packetMap.get(command));
        return packet;
    }


}
