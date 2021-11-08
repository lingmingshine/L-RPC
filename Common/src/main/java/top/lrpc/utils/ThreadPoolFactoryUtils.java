package top.lrpc.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建线程池工具类
 */

@Slf4j
public final class ThreadPoolFactoryUtils {

    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtils() {
    }

    public static ExecutorService createCustomerThreadPookIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomerThreadPookIfAbsent(customThreadPoolConfig, threadNamePrefix, false);

    }

    private static ExecutorService createCustomerThreadPookIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix) {
        return createCustomerThreadPookIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    private static ExecutorService createCustomerThreadPookIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean b) {
        //computeIfAbsent:对 hashMap 中指定 key 的值进行重新计算，如果不存在这个 key，则添加到 hashMap 中
        //hashmap.computeIfAbsent(K key, Function remappingFunction)
        //映射函数
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix,
                k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, b));

        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, b);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    //关闭全部线程池
    public static void shutDownAllThreadPool() {
        log.info("关闭全部的线程方法");
        //并行流，遍历关闭
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("关闭线程池[{}][{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("线程池停止出现问题");
                executorService.shutdown();
            }
        });
    }

    //创建线程池的主要方法
    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean b) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, b);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(),
                customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(),
                customThreadPoolConfig.getUnit(),
                customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    //线程自定义命名
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean b) {
        if (threadNamePrefix != null) {
            if (b != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(b).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }


}
