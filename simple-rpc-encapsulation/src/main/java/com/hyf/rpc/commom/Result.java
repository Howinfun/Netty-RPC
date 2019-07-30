package com.hyf.rpc.commom;

import lombok.Data;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/30
 */
@Data
public class Result {
    private boolean success = true;
    private String msg;
    private Object result;
}
