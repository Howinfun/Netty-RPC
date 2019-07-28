package com.hyf.rpc.zookeeper.config;

import com.alibaba.fastjson.JSON;
import com.hyf.rpc.netty.anno.NettyRPCService;
import com.hyf.rpc.netty.properties.NettyProperties;
import com.hyf.rpc.netty.utils.IpUtil;
import com.hyf.rpc.zookeeper.pojo.IPPojo;
import com.hyf.rpc.zookeeper.properties.ZookeeperProperties;
import com.hyf.rpc.zookeeper.utils.ZKUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Howinfun
 * @desc 扫面提供RPC服务的类，并上传到zookeeper里头
 * @date 2019/7/25
 */
@Configuration
public class ZookeeperServicesInitConfig implements ApplicationContextAware, ApplicationListener<ContextClosedEvent> {

    @Autowired
    private NettyProperties nettyProperties;
    @Autowired
    private ZookeeperProperties zookeeperProperties;
    private Thread thread;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ZKUtils zkUtils = new ZKUtils(this.zookeeperProperties);
        String ip = IpUtil.getIp();
        Integer port = nettyProperties.getServerPort();
        zkUtils.start();
        StringBuilder root = new StringBuilder();
        root.append("/").append(ZookeeperProperties.root);
        StringBuilder namespace = new StringBuilder();
        namespace.append("/").append(ZookeeperProperties.root).append("/").append(zookeeperProperties.getNamespace());
        // 遍历带有NettyRPCService注释的服务实现类
        Map<String,Object> beans = applicationContext.getBeansWithAnnotation(NettyRPCService.class);
        if (beans != null && beans.size() > 0) {
            for (Object serviceBean : beans.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(NettyRPCService.class).value().getName();
                String version = serviceBean.getClass().getAnnotation(NettyRPCService.class).version();
                StringBuilder path = new StringBuilder();
                path.append("/").append(interfaceName).append(version);
                zkUtils.createPersistentNodeByRecursion(namespace.toString()+path.toString(), JSON.toJSONString(IPPojo.builder().ip(ip).port(port).build()).getBytes());
            }
        }
        // 根节点下的所有应用列表
        List<String> servers = zkUtils.getChildsByPath(root.toString());
        for (String server : servers) {
            // 获取其他应用下服务数据列表
            if (!zookeeperProperties.getNamespace().equals(server)){
                // 获取服务下面的服务列表
                List<String> services = zkUtils.getChildsByPath(root.toString()+"/"+server);
                for (String service : services) {
                    if (!ZookeeperCache.getServiceList().containsKey(service)){
                        ZookeeperCache.getServiceList().put(service,new HashSet<>(10));
                    }
                    Set<IPPojo> ips = ZookeeperCache.getServiceList().get(service);
                    IPPojo ipPojo = JSON.parseObject(new String(zkUtils.getData(root.toString()+"/"+server+"/"+service)),IPPojo.class);
                    ips.add(ipPojo);
                }
            }
        }
        zkUtils.close();

        // 开启监听线程
        thread = new Thread(new ZookeeperListener(this.zookeeperProperties),"ZookeeperListener-Thread");
        // 守护线程，会在后台一直运行着
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        System.err.println("程序停止");
        // 去除掉该应用提供的服务
        StringBuilder serverPath = new StringBuilder();
        serverPath.append("/").append(ZookeeperProperties.root)
                .append("/").append(zookeeperProperties.getNamespace());
        ZKUtils zkUtils = new ZKUtils(this.zookeeperProperties);
        zkUtils.start();
        zkUtils.delNodeByRecursion(serverPath.toString());
    }
}
