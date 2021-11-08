package top.lrpc.config;

import lombok.extern.slf4j.Slf4j;
import top.lrpc.registry.util.CuratorUtils;
import top.lrpc.utils.ThreadPoolFactoryUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addshutdownhook 清除");

        /**
         * Runtime.getRuntime().addShutdownHook(shutdownHook);
         * 这个方法的含义说明：
         *    这个方法的意思就是在jvm中增加一个关闭的钩子，当jvm关闭的时候，
         *    会执行系统中已经设置的所有通过方法addShutdownHook添加的钩子，
         *    当系统执行完这些钩子后，jvm才会关闭。所以这些钩子可以在jvm关闭的时候进行内存清理、
         *    对象销毁、关闭连接等操作。
         *
         *    这个钩子可以在一下几种场景中被调用：
         *      程序正常退出
         *      使用System.exit()
         *      终端使用Ctrl+C触发的中断
         *      系统关闭
         *      OutOfMemory宕机
         *      使用Kill pid命令干掉进程（注：在使用kill -9 pid时，是不会被调用的）
         *
         *      总结：就是关闭资源，放开资源，防止资源一直被无效占用
         */

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), 9998);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException e) {

            }
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }


}
