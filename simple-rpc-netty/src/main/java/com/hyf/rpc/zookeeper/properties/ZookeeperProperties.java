package com.hyf.rpc.zookeeper.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Howinfun
 * @desc zookeeper配置
 * @date 2019/7/25
 */
@ConfigurationProperties("zookeeper")
@Component
@Data
public class ZookeeperProperties {
    /** 连接地址 */
    private String url;
    /** session超时，单位毫秒 */
    private Integer sessionTimeOut;
    /** 命名空间，避免多个应用节点名称的冲突 */
    private String namespace;
    /** 所有项目提供服务节点的根节点 */
    public static final String root = "root";
}
