package com.hyf.rpc.netty.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @desc 提供给RPC调用的实现
 * @author Howinfun
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NettyRPCService {
    Class<?> value();
    String version() default "1";
}
