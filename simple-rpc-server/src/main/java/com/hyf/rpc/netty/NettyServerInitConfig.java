package com.hyf.rpc.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/18
 */
@Component
public class NettyServerInitConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private NettyServer nettyServer;
    /**
     * 当ApplicationContext初始或刷新完毕触发
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        nettyServer.start();
    }
}
