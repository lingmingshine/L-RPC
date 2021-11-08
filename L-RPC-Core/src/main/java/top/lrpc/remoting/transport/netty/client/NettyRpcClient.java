package top.lrpc.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.lrpc.enums.CompressTypeEnum;
import top.lrpc.enums.SerializationTypeEnum;
import top.lrpc.extension.ExtensionLoader;
import top.lrpc.factory.SingletionFactory;
import top.lrpc.registry.ServiceDiscovery;
import top.lrpc.remoting.transport.RpcRequestTransport;
import top.lrpc.remoting.transport.coder.LRpcMessageDecoder;
import top.lrpc.remoting.transport.dto.LRpcMessage;
import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.remoting.transport.dto.LRpcResponse;
import top.lrpc.remoting.transport.netty.client.constants.RpcConstants;
import top.lrpc.remoting.transport.coder.LRpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {

    /**
     * CompletableFuture
     *
     * 之前future需要等待isDone为true才能知道任务跑完了。或者就是用get方法调用的时候会出现阻塞。
     * 而使用completableFuture的使用就可以用then，when等等操作来防止以上的阻塞和轮询isDone的现象出现。
     *
     * complete()方法，完成异步操作，主动结束，返回future结果
     * complteException,异步执行不正常结果
     *
     */



    private final ServiceDiscovery serviceDiscovery;
    /**
     *
     */
    private final FliterRequest fliterRequest;

    /**
     * Channel的提供，map存储起来
     */
    private final ChannelProvider channelProvider;

    /**
     * Boostrap是开发netty客户端的基础，通过bootstrap的connect方法连接服务器
     * 返回的是ChannelFuture，通过这个可以判断客户端是否连接成功
     * 在调用connect方法前，需要指定EventLoopGroup,channelFactory
     * (不指定这个,就会使用netty默认的channelFactory,但是需要指定channel,
     * 初始channel时,同时会初始化channelFactory),channelHandler
     */
    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                //不指定channelFactory,就需要指定channel,
                // channel初始化时会指定默认的channelFactory。
                .channel(NioSocketChannel.class)
                //指定channelFactory,就不需要指定channel了。
                //.channelFactory(new ReflectiveChannelFactory<Channel>(NioSocketChannel.class))
                .handler(new LoggingHandler(LogLevel.INFO))
                //  超时时间超出那么断开连接
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 心跳检测IdleStateHandler
                        //当设置了readerIdleTime以后，服务端server会每隔readerIdleTime时间去
                        // 检查一次channelRead方法被调用的情况，如果在readerIdleTime时间内
                        // 该channel上的channelRead()方法没有被触发，就会调用userEventTriggered方法。
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new LRpcMessageEncoder());
                        p.addLast(new LRpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });

        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.fliterRequest = SingletionFactory.getInstance(FliterRequest.class);
        this.channelProvider = SingletionFactory.getInstance(ChannelProvider.class);
    }


    /**
     * 这里使用fliterRequest的作用是用一个map<String,CompleteFuture>
     *     把future存起来，然后NettyRpcClientHandler里面读取响应消息之后主动结束，不至于一直在阻塞等待
     * @param lRpcRequest
     * @return
     */
    @Override
    public Object sendRpcRequest(LRpcRequest lRpcRequest) {
        //处理响应信息的任务
        CompletableFuture<LRpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //获取服务地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(lRpcRequest);
        //获取连接
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            fliterRequest.put(lRpcRequest.getRequestId(), resultFuture);
            LRpcMessage rpcMessage = LRpcMessage.builder().data(lRpcRequest)
                    .codec(SerializationTypeEnum.KYRO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            //channel发送消息
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }


    /**
     * 通过服务地址获取连接
     * @param inetSocketAddress
     * @return
     */
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 连接操作
     * @param inetSocketAddress
     * @return
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        //连接，相当于socket.connect
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
}
