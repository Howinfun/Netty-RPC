package com.hyf.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hyf")
public class SimpleRpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleRpcServerApplication.class, args);
    }

}
