package com.hyf.rpc.netty.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class NettyClientFactoryBean implements FactoryBean<Object> {

    private Class<?> type;

    @Override
    public Object getObject() throws Exception {
        // 这里的interfaces注意是就是type，因为我们现在是给接口做代理，千万别写type.getInterfaces(),不然启动会报错
        return Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},new NettyRPCInvocationHandler(this.type));
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }
}
