package com.hyf.rpc.netty.serialize;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
public interface Serializer {

    Serializer DEFAULT = new JSONSerializer();

    Byte getSerializerAlogrithm();

    /**
     * 序列化
     * @param object 被序列化实体
     * @return
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param data 字节数组数据
     * @param T 反序列化实体类型
     * @param <T>
     * @return
     */
    <T> T deSerialize(byte[] data, Class<T> T);
}
