package com.hyf.rpc.serviceImpl;

import com.hyf.rpc.service.HelloService;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello "+name;
    }
}
