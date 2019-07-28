package com.hyf.rpc.netty.common;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/22
 */
@Slf4j
public class ChannelPool {

    /** 将channel缓存起来，key为IP:port  value为channel*/
    private static final Map<String, Channel> channels = new ConcurrentHashMap<>(10);

    public static Channel getChannel(String serverId){
        return channels.get(serverId);
    }
    public static void addChannel(String serverId,Channel channel){
        if (!channels.containsKey(serverId)){
            channels.put(serverId,channel);
        }
        log.info("Channel列表：数量->"+channels.size()+" 列表->"+channels.toString());
    }
    public static void removeChannel(String serverId){
        if (channels.containsKey(serverId)){
            channels.remove(serverId);
        }
        log.info("Channel列表：数量->"+channels.size()+" 列表->"+channels.toString());
    }
    public static boolean containChannel(String serverId){
        return channels.containsKey(serverId);
    }

    public static Channel getChannelByContainKey(List<String> ips){
        Channel channel = null;
        for (String ip : ips) {
            if (channels.containsKey(ip)){
                channel = getChannel(ip);
                break;
            }
        }
        return channel;
    }
}
