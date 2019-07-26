package com.hyf.rpc.netty.common;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/22
 */
public class ChannelPool {

    /** 将channel缓存起来，key为IP:port  value为channel*/
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

    public static String getContainKey(List<String> ips){
        for (String ip : ips) {
            if (channels.containsKey(ip)){
                return ip;
            }
        }
        return null;
    }
}
