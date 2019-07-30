package com.hyf.rpc.controller;

import com.hyf.rpc.netty.common.Result;
import com.hyf.rpc.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@RestController()
@RequestMapping("/hello")
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    @Autowired
    private HelloService helloService;

    @GetMapping("/sayHello")
    public Result sayHello(@RequestParam String name){
        Result result = new Result();
        try {
            result.setResult(helloService.sayHello(name));
            return  result;
        }catch (Exception e){
            result.setSuccess(false);
            return result;
        }

    }

}
