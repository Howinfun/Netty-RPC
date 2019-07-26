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
    private String clientIp="127.0.0.1";
    private Integer clientPort=1000;
    private Integer serverPort=1001;
}
