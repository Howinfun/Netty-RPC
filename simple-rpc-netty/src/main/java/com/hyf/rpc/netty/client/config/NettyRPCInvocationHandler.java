package com.hyf.rpc.netty.client.config;

import com.hyf.rpc.netty.anno.NettyRPC;
import com.hyf.rpc.netty.client.NettyClient;
import com.hyf.rpc.netty.packet.RPCRequestPacket;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@NoArgsConstructor
@Component
public class NettyRPCInvocationHandler implements InvocationHandler {

    private Class<?> type;

    public NettyRPCInvocationHandler(Class<?> type){
        this.type = type;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequestPacket requestPacket = new RPCRequestPacket();
        String version = type.getAnnotation(NettyRPC.class).version();
        requestPacket.setClazz(type);
        requestPacket.setVersion(version);
        requestPacket.setMethodName(method.getName());
        requestPacket.setParamTypes(method.getParameterTypes());
        requestPacket.setParams(args);
        Object result = NettyClient.callRPC(requestPacket);
        return result;
    }
}
