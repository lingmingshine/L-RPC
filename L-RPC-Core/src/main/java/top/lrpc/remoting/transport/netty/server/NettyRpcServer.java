package top.lrpc.remoting.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.lrpc.config.CustomShutdownHook;
import top.lrpc.config.RpcServiceConfig;
import top.lrpc.factory.SingletionFactory;
import top.lrpc.provider.ServerProvider;
import top.lrpc.provider.impl.ZKServiceProviderImpl;
import top.lrpc.remoting.transport.coder.LRpcMessageDecoder;
import top.lrpc.remoting.transport.coder.LRpcMessageEncoder;
import top.lrpc.utils.RuntimeUtil;
import top.lrpc.utils.ThreadPoolFactoryUtils;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyRpcServer {

    public static final int PORT = 9998;

    private final ServerProvider serviceProvider =
            SingletionFactory.getInstance(ZKServiceProviderImpl.class);

    //类似socketServer
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }


    @SneakyThrows
    public void start() {
        //启用钩子，在socket连接的过程中出现失败的时候，在jvm关闭之前处理关闭一些资源：比如线程池，注册中心
        CustomShutdownHook.getCustomShutdownHook().clearAll();

        String host = InetAddress.getLocalHost().getHostAddress();
        //两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //根据cpu情况创建线程数量和自定义线程名称
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false)
        );
        try {
            //服务器bootstrap
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。
                    // TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,
                    // 如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new LRpcMessageEncoder());
                            p.addLast(new LRpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });

            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(host, PORT).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }

}
