package com.hyf.rpc.netty.attributes;

import io.netty.util.AttributeKey;

/**
 * @author howinfun
 * @version 1.0
 * @desc 标识
 * @date 2019/7/28
 * @company WCWC
 */
public interface Attributes {
    /**  应用的IP+PORT */
    AttributeKey<String> IP_PORT = AttributeKey.newInstance("IP_PORT");
}
