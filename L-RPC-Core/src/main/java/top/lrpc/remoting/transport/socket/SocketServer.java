package top.lrpc.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import top.lrpc.config.CustomShutdownHook;
import top.lrpc.config.RpcServiceConfig;
import top.lrpc.factory.SingletionFactory;
import top.lrpc.provider.ServerProvider;
import top.lrpc.provider.impl.ZKServiceProviderImpl;
import top.lrpc.utils.ThreadPoolFactoryUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * 服务器端：
 * 1.创建ServerSocket对象并且绑定地址IP和端口号port
 * server.bind(new InetSocketAddress(host,port)
 * 2.通过accept方法监听客户请求
 * 3.建立连接之后，通过输入流读取客户端发送的请求信息
 * 4.通过输出流向客户端发送响应消息
 * 5.关闭相关资源
 *
 * 客户端-服务端 多对一情况下需要创建多个线程
 * 通过使用线程池来利用资源创建线程来保证资源的高效利用
 */

@Slf4j
public class SocketServer {

    private final ExecutorService threadPool;

    private final ServerProvider serverProvider;

    public SocketServer() {
        //创建线程池
        threadPool = ThreadPoolFactoryUtils.createCustomerThreadPookIfAbsent("socket-server-rpc-pool");
        serverProvider = SingletionFactory.getInstance(ZKServiceProviderImpl.class);
    }


    public void registryService(RpcServiceConfig rpcServiceConfig) {
        serverProvider.publishService(rpcServiceConfig);
    }


    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, 9998));
            //在socket连接的过程中出现失败的时候，在jvm关闭之前处理关闭一些资源：比如线程池，注册中心
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            //多线程处理线程
            while ((socket = server.accept()) != null) {
                log.info("客户端连接中[{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("io异常", e);
        }
    }
}
