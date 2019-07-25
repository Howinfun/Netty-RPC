package com.hyf.rpc.zookeeper.config;

import com.hyf.rpc.zookeeper.pojo.IPPojo;
import com.hyf.rpc.zookeeper.properties.ZookeeperProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/25
 */
@Component
public class ZookeeperListener implements DisposableBean,Runnable {
    @Autowired
    private ZookeeperProperties zookeeperProperties;
    private Thread thread;
    private CuratorFramework zkClient;
    /** 存储提供服务列表 key->服务名 value->提供服务的ip:port */
    public static final Map<String, Set<IPPojo>> serviceList = new HashMap<>(10);

    /**
     * 在构造函数启动线程进行监听
     */
    public ZookeeperListener(){
        /*thread = new Thread(this,"Thread-ZookeeperListener");
        thread.start();*/
    }

    @Override
    public void run() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,10);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperProperties.getUrl())
                .sessionTimeoutMs(zookeeperProperties.getSessionTimeOut())
                .namespace(ZookeeperProperties.root+"/"+zookeeperProperties.getNamespace())
                .retryPolicy(retryPolicy).build();
        zkClient.start();


    }

    @Override
    public void destroy() throws Exception {
        zkClient.close();
    }
}
