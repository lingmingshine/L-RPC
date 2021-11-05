package top.lrpc;

import top.lrpc.config.RpcServiceConfig;
import top.lrpc.proxy.RpcClientProxy;
import top.lrpc.remoting.transport.socket.SocketClient;
import top.lrpc.remoting.transport.RpcRequestTransport;

public class SocketClientMain {

    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy clientProxy = new RpcClientProxy(rpcRequestTransport,rpcServiceConfig);
        Hello hello1 = clientProxy.getProxy(Hello.class);
        String helloMsg = hello1.hello(new HelloE("111","aa"));
        System.out.println(helloMsg);
    }
}
