package com.hyf.rpc;

import com.hyf.rpc.netty.client.config.NettyRpcClientRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({NettyRpcClientRegistrar.class})
public class SimpleRpcClientApplication{

    public static void main(String[] args) {
        SpringApplication.run(SimpleRpcClientApplication.class, args);
    }
}
