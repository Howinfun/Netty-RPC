package com.hyf.rpc.netty.packet;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */

@Getter
public abstract class Packet {
    /** 魔数 **/
    @JSONField(serialize = false,deserialize = false)
    public static final int MAGIC_NUMBER = 0x12345678;
    @JSONField(serialize = false,deserialize = false)
    public static final Byte VERSION = 1;

    /**
     * 获取指令
     * @return
     */
    public abstract Byte getCommand();
}
