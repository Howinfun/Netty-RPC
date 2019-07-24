package com.hyf.rpc.serviceImpl;

import com.hyf.rpc.netty.anno.NettyRPCService;
import com.hyf.rpc.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@NettyRPCService(value = HelloService.class,version = "1")
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello "+name;
    }
}
