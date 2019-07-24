package com.hyf.rpc;

import com.hyf.rpc.netty.config.NettyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.hyf")
@EnableConfigurationProperties({NettyConfig.class})
public class SimpleRpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleRpcServerApplication.class, args);
    }

}
