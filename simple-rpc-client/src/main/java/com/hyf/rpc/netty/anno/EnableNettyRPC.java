package com.hyf.rpc.netty.anno;

import com.hyf.rpc.netty.config.NettyRpcClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(NettyRpcClientRegistrar.class)
public @interface EnableNettyRPC {
    //扫描的包名，如果为空，则根据启动类所在的包名扫描
    String[] basePackages() default {};
}
