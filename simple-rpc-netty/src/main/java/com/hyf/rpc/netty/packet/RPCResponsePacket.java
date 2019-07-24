package com.hyf.rpc.netty.packet;

import com.hyf.rpc.netty.command.Command;
import lombok.Data;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Data
public class RPCResponsePacket extends Packet{

    private boolean success;
    private Object result;

    @Override
    public Byte getCommand() {
        return Command.RPC_RESPONSE;
    }
}
