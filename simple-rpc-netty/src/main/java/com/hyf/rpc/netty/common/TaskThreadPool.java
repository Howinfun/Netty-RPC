package com.hyf.rpc.netty.common;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/12
 */
@Slf4j
public class TaskThreadPool {

    public static final TaskThreadPool INSTANCE  = new TaskThreadPool();
    private final ThreadPoolExecutor executor;
    private TaskThreadPool(){
        /**
         * 核心线程数：10
         * 最大线程数：20
         * 线程保持活跃时间：60s
         * 队列：阻塞队列，最多存放100个任务
         * 拒绝策略：任务将被放弃
         */
        this.executor = new ThreadPoolExecutor(10,
                                            20,
                                            60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),new ThreadPoolExecutor.CallerRunsPolicy());
    }
    public Future submit(Runnable task){
        log.info("业务线程池执行任务中...");
        Future future = executor.submit(task);
        return future;
    }
}
