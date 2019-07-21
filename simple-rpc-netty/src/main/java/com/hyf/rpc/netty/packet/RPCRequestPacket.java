package com.hyf.rpc.netty.packet;

import com.hyf.rpc.netty.command.Command;
import lombok.Data;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Data
public class RPCRequestPacket extends Packet{

    /** 调用方法名 */
    private String methodName;
    /** 调用服务的实现类class */
    private Class<?> clazz;
    /**
     * 调用方法的参数列表类型
     */
    private Class[] paramTypes;
    /**
     * 调用服务传参
     */
    private Object[] params;

    @Override
    public Byte getCommand() {
        return Command.RPC_REQUEST;
    }
}
