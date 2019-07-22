package com.hyf.rpc.netty.common;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/22
 */
public class ChannelPool {

    private static final Map<String, Channel> channels = new ConcurrentHashMap<>(10);

    public static Channel getChannel(String clientId){
        return channels.get(clientId);
    }
    public static void addChannel(String clientId,Channel channel){
        if (!channels.containsKey(clientId)){
            channels.put(clientId,channel);
        }
    }
    public static void removeChannel(String clientId){
        if (channels.containsKey(clientId)){
            channels.remove(clientId);
        }
    }
    public static boolean containChannel(String clientId){
        return channels.containsKey(clientId);
    }
}
