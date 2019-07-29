package com.hyf.rpc.netty.common;

import lombok.Data;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/29
 */
@Data
public class Result {
    /** 是否成功 */
    private boolean success = true;
    /** 返回信息 */
    private String msg;
    /** 返回结果 */
    private Object result;
}
