package com.hyf.rpc;

import com.hyf.rpc.netty.anno.EnableNettyRPC;
import com.hyf.rpc.netty.client.config.NettyRpcClientRegistrar;
import com.hyf.rpc.netty.config.NettyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties({NettyConfig.class})
@EnableNettyRPC(basePackages = {"com.hyf.rpc.service"})
@Import({NettyRpcClientRegistrar.class})
public class SimpleRpcClientApplication{

    public static void main(String[] args) {
        SpringApplication.run(SimpleRpcClientApplication.class, args);
    }
}
