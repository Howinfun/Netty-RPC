package com.hyf.rpc.netty.server.handler;

import com.hyf.rpc.netty.common.TaskThreadPool;
import com.hyf.rpc.netty.packet.RPCRequestPacket;
import com.hyf.rpc.netty.packet.RPCResponsePacket;
import com.hyf.rpc.netty.server.config.NettyServerInitConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        TaskThreadPool.INSTANCE.submit(() -> {
            RPCResponsePacket responsePacket = new RPCResponsePacket();
            // 根据请求信息调用方法
            Class clazz = msg.getClazz();
            String version = msg.getVersion();
            String methodName = msg.getMethodName();
            Object[] params = msg.getParams();
            Class[] paramTypes = msg.getParamTypes();
            if (NettyServerInitConfig.beanMap.isEmpty()){
                responsePacket.setSuccess(false);
                responsePacket.setResult("无服务提供");
            }else {
                String key = clazz.getName()+version;
                if (NettyServerInitConfig.beanMap.containsKey(key)){
                    Object serviceBean = NettyServerInitConfig.beanMap.get(key);
                    Class serviceClazz = serviceBean.getClass();
                    Method method = null;
                    try {
                        method = serviceClazz.getMethod(methodName,paramTypes);
                    } catch (NoSuchMethodException e) {
                        responsePacket.setSuccess(false);
                        responsePacket.setResult("调用失败");
                    }
                    Object result = null;
                    try {
                        result = method.invoke(serviceClazz.newInstance(),params);
                    } catch (IllegalAccessException | InvocationTargetException |InstantiationException e) {
                        responsePacket.setSuccess(false);
                        responsePacket.setResult("调用失败");
                    }
                    responsePacket.setSuccess(true);
                    responsePacket.setResult(result);
                }else {
                    responsePacket.setSuccess(false);
                    responsePacket.setResult("无服务提供");
                }
            }
            ctx.channel().writeAndFlush(responsePacket);
        });
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 如果出现异常，则关闭体通道
        ctx.channel().close();
    }
}
