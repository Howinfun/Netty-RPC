package com.hyf.rpc.netty.client.config;

import com.hyf.rpc.netty.anno.NettyRPC;
import com.hyf.rpc.netty.client.NettyClient;
import com.hyf.rpc.netty.common.Result;
import com.hyf.rpc.netty.exception.SimpleRpcException;
import com.hyf.rpc.netty.packet.RPCRequestPacket;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Howinfun
 * @desc 动态代理
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
        Result result = NettyClient.callRPC(requestPacket);
        if (result.isSuccess()){
            return result.getResult();
        }else{
            // 如果RPC通信失败，抛出自定义异常
            throw new SimpleRpcException(result.getMsg());
        }
    }
}
