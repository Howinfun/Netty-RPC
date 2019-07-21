package com.hyf.rpc.netty.command;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
public interface Command {
    Byte RPC_REQUEST = 1;
    Byte RPC_RESPONSE = 2;
}
