package top.lrpc;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import top.lrpc.annotation.LScan;
import top.lrpc.config.RpcServiceConfig;
import top.lrpc.impl.HelloServiceImpl;
import top.lrpc.remoting.transport.netty.server.NettyRpcServer;

@LScan(basePackage = {"top.lrpc"})
public class NettyServerMain {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        Hello helloService2 = new HelloServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(helloService2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
