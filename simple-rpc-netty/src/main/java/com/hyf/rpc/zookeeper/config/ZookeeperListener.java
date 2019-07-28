
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

import java.util.List;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/25
 */
public class ZookeeperListener implements Runnable{

    private ZookeeperProperties zookeeperProperties;
    private CuratorFramework zkClient;
    private ZKUtils zkUtils;

    public ZookeeperListener(ZookeeperProperties zookeeperProperties){
        this.zookeeperProperties = zookeeperProperties;
    }

    @Override
    public void run() {
        try {
            // 根节点下的所有应用列表
            StringBuilder root = new StringBuilder();
            root.append("/").append(ZookeeperProperties.root);
            this.zkUtils = new ZKUtils(this.zookeeperProperties);
            this.zkUtils.start();
            List<String> servers = zkUtils.getChildsByPath(root.toString());
            // 弄一个zkClient来进行监听操作
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,10);
            this.zkClient = CuratorFrameworkFactory.builder()
                    .connectString(zookeeperProperties.getUrl())
                    .sessionTimeoutMs(zookeeperProperties.getSessionTimeOut())
                    //.namespace(ZookeeperProperties.root+"/"+zookeeperProperties.getNamespace())
                    .retryPolicy(retryPolicy).build();

            this.zkClient.start();

            // 监听根节点，如果根节点下增加新应用或者删除应用
            addRootListener(root.toString(),this.zkClient,this.zkUtils);
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("zookeeper监听失败:"+e.getMessage());
        }
    }

    private void addRootListener(String root,CuratorFramework zkClient,ZKUtils zkUtils) throws Exception{
        PathChildrenCache cache = new PathChildrenCache(zkClient,root,true);
        // 在初始化时就开始进行监听
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch(event.getType()){
                    case CHILD_ADDED:
                        String serverPath = event.getData().getPath();
                        System.out.println("/root->新增子节点："+serverPath+"");
                        // 获取此应用下面的服务列表
                        List<String> childPath = zkUtils.getChildsByPath(serverPath);
                        for (String s : childPath) {
                            // 查询数据
                            IPPojo ip = JSON.parseObject(new String(zkUtils.getData(serverPath+"/"+s)),IPPojo.class);
                            ZookeeperCache.addService(s,ip);
                        }
                        //监听Server
                        addServerListener(serverPath,client);
                        break;
                    case CHILD_UPDATED:
                        System.out.println("/root->子节点："+event.getData().getPath()+",数据修改为："+new String(event.getData().getData()));
                        break;
                    case CHILD_REMOVED:
                        String serverPath2 = event.getData().getPath();
                        System.out.println("子节点："+serverPath2+"被删除");
                        // 获取此应用下面的服务列表 不能这么玩，节点都没了，肯定是查的是空，所以最好是如果有子节点，节点不能删除
                        /*List<String> childPath2 = zkUtils.getChildsByPath(serverPath2);
                        for (String s : childPath2) {
                            // 查询数据
                            IPPojo ip = JSON.parseObject(new String(zkUtils.getData(serverPath2+"/"+s)),IPPojo.class);
                            delService(s,ip);
                        }*/
                        // 从监听列表中移除
                        ZookeeperCache.removeListener(serverPath2);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void addServerListener(String path,CuratorFramework zkClient) throws Exception{
        PathChildrenCache cache = new PathChildrenCache(zkClient,path,true);
        // 增加到监听列表
        ZookeeperCache.addListener(path,cache);
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
                        ZookeeperCache.addService(childPath,ipPojo);
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
                        ZookeeperCache.delService(childPath2,ipPojo2);
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
