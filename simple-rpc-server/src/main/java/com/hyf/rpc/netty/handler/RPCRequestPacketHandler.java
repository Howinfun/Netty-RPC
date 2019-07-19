package com.hyf.rpc.netty.handler;

import com.hyf.rpc.netty.packet.RPCRequestPacket;
import com.hyf.rpc.netty.packet.RPCResponsePacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/16
 */
@ChannelHandler.Sharable
public class RPCRequestPacketHandler extends SimpleChannelInboundHandler<RPCRequestPacket> {

    public static final RPCRequestPacketHandler INSTANCE = new RPCRequestPacketHandler();
    private RPCRequestPacketHandler(){}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequestPacket msg) throws Exception {
        RPCResponsePacket responsePacket = new RPCResponsePacket();
        // 获取rpc调用信息，利用反射执行方法，返回结果
        Class clazz = msg.getClazz();
        String methodName = msg.getMethodName();
        Object[] params = msg.getParams();
        Class[] paramTypes = msg.getParamTypes();
        // 扫面路径下所有元数据
        Reflections reflections = new Reflections("com.hyf.rpc.serviceImpl");
        Set<Class> subTypes = reflections.getSubTypesOf(clazz);
        if (subTypes.isEmpty()){
            responsePacket.setSuccess(false);
            responsePacket.setMsg("没有实现类");
        }else if (subTypes.size() > 1){
            responsePacket.setSuccess(false);
            responsePacket.setMsg("多个实现类，无法判断执行哪一个");
        }else{
            Class subClass = subTypes.toArray(new Class[1])[0];
            Method method = subClass.getMethod(methodName,paramTypes);
            Object result = method.invoke(subClass.newInstance(),params);
            responsePacket.setSuccess(true);
            responsePacket.setResult(result);
        }
        ctx.channel().writeAndFlush(responsePacket);
    }
}
