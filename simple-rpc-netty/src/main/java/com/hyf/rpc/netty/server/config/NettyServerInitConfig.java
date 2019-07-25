package com.hyf.rpc.netty.server.config;

import com.hyf.rpc.netty.anno.NettyRPCService;
import com.hyf.rpc.netty.properties.NettyProperties;
import com.hyf.rpc.netty.server.NettyServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Howinfun
 * @desc Netty服务提供者启动&扫面存储提供服务的实现类
 * @date 2019/7/18
 */
@Component
public class NettyServerInitConfig implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    /** 提供RPC服务的实现类  key为接口名+版本号，value为bean实例*/
    public static final Map<String,Object> beanMap = new HashMap<>(10);

    @Autowired
    private NettyServer nettyServer;
    @Autowired
    private NettyProperties nettyProperties;
    /**
     * 当ApplicationContext初始或刷新完毕触发
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (nettyProperties.getServerPort() != null){
            nettyServer.start();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 遍历带有NettyRPCService注释的服务实现类
        Map<String,Object> beans = applicationContext.getBeansWithAnnotation(NettyRPCService.class);
        if (beans != null && beans.size() > 0) {
            for (Object serviceBean : beans.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(NettyRPCService.class).value().getName();
                String version = serviceBean.getClass().getAnnotation(NettyRPCService.class).version();
                beanMap.put(interfaceName+version, serviceBean);
            }
        }
        System.out.println(beanMap.toString());

    }
}
