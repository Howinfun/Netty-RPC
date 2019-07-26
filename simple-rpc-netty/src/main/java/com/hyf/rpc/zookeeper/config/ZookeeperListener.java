package com.hyf.rpc.zookeeper.config;

import com.alibaba.fastjson.JSON;
import com.hyf.rpc.zookeeper.pojo.IPPojo;
import com.hyf.rpc.zookeeper.properties.ZookeeperProperties;
import com.hyf.rpc.zookeeper.utils.ZKUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/25
 */
public class ZookeeperListener implements Runnable {

    private ZookeeperProperties zookeeperProperties;
    private CuratorFramework zkClient;

    /** 存储提供服务列表 key->服务名 value->提供服务的ip:port,用set保存，能帮我们去重，不过IPPojo记得重写equals和hasCode */
    public static final Map<String, Set<IPPojo>> serviceList = new HashMap<>(10);

    /**
     * 在构造函数启动线程进行监听
     */
    public ZookeeperListener(ZookeeperProperties zookeeperProperties){
        this.zookeeperProperties = zookeeperProperties;
    }

    @Override
    public void run() {
        try {
            // 根节点下的所有应用列表
            StringBuilder root = new StringBuilder();
            root.append("/").append(ZookeeperProperties.root);
            ZKUtils zkUtils = new ZKUtils(this.zookeeperProperties);
            zkUtils.start();
            List<String> servers = zkUtils.getChildsByPath(root.toString());
            zkUtils.close();
            // 弄一个zkClient来进行监听操作
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,10);
            zkClient = CuratorFrameworkFactory.builder()
                    .connectString(zookeeperProperties.getUrl())
                    .sessionTimeoutMs(zookeeperProperties.getSessionTimeOut())
                    //.namespace(ZookeeperProperties.root+"/"+zookeeperProperties.getNamespace())
                    .retryPolicy(retryPolicy).build();

            zkClient.start();
            // 监听其他应用下的服务节点变更
            for (String server : servers) {
                if (!zookeeperProperties.getNamespace().equals(server)){
                    String serverPath = root+"/"+server;
                    // 监听
                    addServerListener(serverPath,zkClient);
                }
            }

            // 继续监听根节点，如果根节点下增加新应用或者删除应用
            PathChildrenCache cache = new PathChildrenCache(zkClient,root.toString(),true);
            cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            cache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    switch(event.getType()){
                        case CHILD_ADDED:
                            String serverPath = event.getData().getPath();
                            // 获取此应用下面的服务列表
                            List<String> childPath = zkUtils.getChildsByPath(serverPath);
                            for (String s : childPath) {
                                // 查询数据
                                IPPojo ip = JSON.parseObject(new String(zkUtils.getData(serverPath+"/"+s)),IPPojo.class);

                            }
                            break;
                        case CHILD_UPDATED:
                            System.out.println("子节点："+event.getData().getPath()+",数据修改为："+new String(event.getData().getData()));
                            break;
                        case CHILD_REMOVED:

                            break;
                        default:
                            break;
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("zookeeper监听失败:"+e.getMessage());
        }
    }

    private void addServerListener(String path,CuratorFramework zkClient) throws Exception{
        PathChildrenCache cache = new PathChildrenCache(zkClient,path,true);
        // 在初始化时就开始进行监听
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch(event.getType()){
                    case CHILD_ADDED:
                        String[] childPathArr = event.getData().getPath().split("/");
                        String childPath = childPathArr[childPathArr.length-1];
                        byte[] childData = event.getData().getData();
                        IPPojo ipPojo = JSON.parseObject(new String(childData),IPPojo.class);
                        System.out.println("新增子节点："+event.getData().getPath()+",数据为："+ipPojo);
                        if (ZookeeperListener.serviceList.containsKey(childPath)){
                            Set<IPPojo> ips = ZookeeperListener.serviceList.get(childPath);
                            ips.add(ipPojo);
                        }else{
                            Set<IPPojo> ips = new HashSet<>(10);
                            ips.add(ipPojo);
                            ZookeeperListener.serviceList.put(childPath,ips);
                        }
                        break;
                    case CHILD_UPDATED:
                        System.out.println("子节点："+event.getData().getPath()+",数据修改为："+new String(event.getData().getData()));
                        break;
                    case CHILD_REMOVED:
                        String[] childPathArr2 = event.getData().getPath().split("/");
                        String childPath2 = childPathArr2[childPathArr2.length-1];
                        byte[] childData2 = event.getData().getData();
                        IPPojo ipPojo2 = JSON.parseObject(new String(childData2),IPPojo.class);
                        System.out.println("子节点："+event.getData().getPath()+"被删除");
                        if (ZookeeperListener.serviceList.containsKey(childPath2)){
                            Set<IPPojo> ips = ZookeeperListener.serviceList.get(childPath2);
                            if (ips.contains(ipPojo2)){
                                ips.remove(ipPojo2);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public static void main(String[] args) {
        String path = "/root/rpc-client/userService";
        String[] arr = path.split("/");
        System.out.println(arr[arr.length-1]);
    }
}
