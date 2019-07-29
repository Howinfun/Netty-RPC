package com.hyf.rpc.netty.exception;

/**
 * @author Howinfun
 * @desc 自定义异常
 * @date 2019/7/29
 */
public class SimpleRpcException extends RuntimeException{
    public SimpleRpcException(String message) {
        super(message);
    }

    public SimpleRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public SimpleRpcException(Throwable cause) {
        super(cause);
    }

    protected SimpleRpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
