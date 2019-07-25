package com.hyf.rpc.zookeeper.utils;

import com.hyf.rpc.zookeeper.properties.ZookeeperProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/24
 */
public class ZKUtils {

    private CuratorFramework zkClient;

    public ZKUtils(ZookeeperProperties zookeeperProperties){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,10);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperProperties.getUrl())
                .sessionTimeoutMs(zookeeperProperties.getSessionTimeOut())
                //.namespace(ZookeeperConfig.root+"/"+zookeeperProperties.getNamespace())
                .retryPolicy(retryPolicy).build();
    }

    /**
     * 启动ZookeeperClient
     */
    public void start(){
        zkClient.start();
    }

    /**
     * 关闭ZookeeperClient
     */
    public void close(){
        zkClient.close();
    }

    /**
     * 创建临时节点
     * @param path
     * @param data
     * @return
     */
    public boolean createEphemeralNode(String path,byte[] data){
        try {
            if (!checkExisit(path)){
                zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path,data);
            }else{
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 递归创建临时节点
     * @param path
     * @param data
     * @return
     */
    public boolean createEphemeralNodeByRecursion(String path,byte[] data){
        try {
            if (!checkExisit(path)){
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,data);
            }else{
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 创建持久节点
     * @param path
     * @param data
     * @return
     */
    public boolean createPersistentNode(String path,byte[] data){
        try {
            if (!checkExisit(path)){
                zkClient.create().withMode(CreateMode.PERSISTENT).forPath(path,data);
            }else{
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
    /**
     * 递归创建持久节点
     * @param path
     * @param data
     * @return
     */
    public boolean createPersistentNodeByRecursion(String path,byte[] data){
        try {
            if (!checkExisit(path)){
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,data);
            }else{
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 修改节点数据
     * @param path
     * @param data
     * @return
     */
    public boolean setData(String path,byte[] data){
        try {
            if (checkExisit(path)){
                zkClient.setData().forPath(path,data);
            }else{
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 获取数据
     * @param path
     * @return
     */
    public byte[] getData(String path){
        try {
            if (checkExisit(path)){
                return zkClient.getData().forPath(path);
            }else{
                return null;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * 递归删除节点
     * @param path
     * @return
     */
    public boolean delNodeByRecursion(String path){
        try {
            if (checkExisit(path)){
                zkClient.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            }else{
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 删除节点
     * @param path
     * @return
     */
    public boolean delNode(String path){
        try {
            zkClient.delete().guaranteed().forPath(path);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 判断节点是否存在
     * @param path
     * @return
     */
    public boolean checkExisit(String path){
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null){
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 根据路径获取所有子节点
     * @param path
     * @return
     */
    public List<String> getChildsByPath(String path){
        List list = new ArrayList(10);
        try {
            list = zkClient.getChildren().forPath(path);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return list;
        }
        return list;
    }

}
