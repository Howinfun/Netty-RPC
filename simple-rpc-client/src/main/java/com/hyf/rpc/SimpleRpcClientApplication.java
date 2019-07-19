package com.hyf.rpc;

import com.hyf.rpc.netty.anno.EnableNettyRPC;
import com.hyf.rpc.netty.config.NettyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({NettyConfig.class})
@EnableNettyRPC(basePackages = {"com.hyf.rpc"})
public class SimpleRpcClientApplication{

    public static void main(String[] args) {
        SpringApplication.run(SimpleRpcClientApplication.class, args);
    }
}
