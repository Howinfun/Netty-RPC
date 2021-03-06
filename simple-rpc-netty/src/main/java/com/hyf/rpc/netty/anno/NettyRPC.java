package com.hyf.rpc.netty.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @desc 使用RPC接口
 * @author Howinfun
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NettyRPC {
    String version() default "1";
}
