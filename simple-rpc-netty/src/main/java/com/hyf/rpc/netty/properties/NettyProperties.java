package com.hyf.rpc.netty.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/25
 */
@ConfigurationProperties("netty")
@Component
@Data
public class NettyProperties {
    private String clientIp;
    private Integer clientPort;
    private Integer serverPort;
}
