package com.hyf.rpc.netty.config;

import com.hyf.rpc.netty.NettyClient;
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
        requestPacket.setClazz(type);
        requestPacket.setMethodName(method.getName());
        requestPacket.setParamTypes(method.getParameterTypes());
        requestPacket.setParams(args);
        Object result = NettyClient.callRPC(requestPacket);
        return result;
    }
}
