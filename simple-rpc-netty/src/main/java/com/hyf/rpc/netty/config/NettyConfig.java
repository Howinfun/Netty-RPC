package com.hyf.rpc.netty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/16
 */
@ConfigurationProperties("netty")
@Data
public class NettyConfig {
    private String clientIp;
    private Integer clientPort;
    private Integer serverPort;
}
