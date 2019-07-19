package com.hyf.rpc.service;

import com.hyf.rpc.netty.anno.NettyRPC;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@NettyRPC()
public interface HelloService {

    String sayHello(String name);
}
