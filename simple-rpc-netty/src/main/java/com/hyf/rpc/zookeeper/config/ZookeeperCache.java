package com.hyf.rpc.zookeeper.config;

import com.hyf.rpc.zookeeper.pojo.IPPojo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author howinfun
 * @version 1.0
 * @desc
 * @date 2019/7/27
 * @company WCWC
 */
@Slf4j
public class ZookeeperCache{

    /** 存储提供服务列表 key->服务名 value->提供服务的ip:port,用set保存，能帮我们去重，不过IPPojo记得重写equals和hasCode */
    private static final Map<String, Set<IPPojo>> serviceList = new ConcurrentHashMap<>(10);
    /**  服务监听列表，当新增服务则添加到这，如果服务关闭，则cloase掉再移除掉 */
    private static final Map<String, PathChildrenCache> listenerList = new ConcurrentHashMap<>(10);

    /**
     * 获取服务列表
     * @param
     * @return
     */
    public static Map<String, Set<IPPojo>> getServiceList(){
        return serviceList;
    }

    /**
     * 移除IP
     * @param ip
     */
    public static void removeIPPojo(String ip){
        String[] ipArr = ip.split(":");
        IPPojo ipPojo = IPPojo.builder().ip(ipArr[0]).port(Integer.parseInt(ipArr[1])).build();
        serviceList.forEach((key,value)->{
            value.remove(ipPojo);
        });
    }

    /**
     * 增加服务
     * @param servicePath
     * @param ip
     */
    public static  void addService(String servicePath,IPPojo ip){
        // 判断缓存的是否有serverPath服务
        if (serviceList.containsKey(servicePath)){
            Set<IPPojo> ips = serviceList.get(servicePath);
            ips.add(ip);
        }else{
            Set<IPPojo> ips = new HashSet<>(10);
            ips.add(ip);
            serviceList.put(servicePath,ips);
        }
        log.info("服务列表：数量->"+serviceList.size()+" 列表->"+serviceList.toString());
    }

    /**
     * 移除服务
     * @param servicePath
     * @param ip
     */
    public static  void delService(String servicePath,IPPojo ip){
        if (serviceList.containsKey(servicePath)){
            Set<IPPojo> ips = serviceList.get(servicePath);
            if (ips.contains(ip)){
                ips.remove(ip);
            }
        }
        log.info("服务列表：数量->"+serviceList.size()+" 列表->"+serviceList.toString());
    }

    /**
     * 增加监听
     * @param serverPath
     * @param cache
     */
    public static void addListener(String serverPath,PathChildrenCache cache){
        if (!listenerList.containsKey(serverPath)){
            listenerList.put(serverPath,cache);
        }
        log.info("监听列表：数量->"+listenerList.size()+" 列表->"+listenerList.toString());
    }

    /**
     * 移除监听
     * @param serverPath
     */
    public static void removeListener(String serverPath){
        if (listenerList.containsKey(serverPath)){
            PathChildrenCache cache = listenerList.get(serverPath);
            try {
                cache.close();
                listenerList.remove(serverPath);
                log.info("监听列表：数量->"+listenerList.size()+" 列表->"+listenerList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
