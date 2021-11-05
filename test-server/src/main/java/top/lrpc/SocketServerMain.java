package top.lrpc;

import top.lrpc.config.RpcServiceConfig;
import top.lrpc.impl.HelloServiceImpl;
import top.lrpc.remoting.transport.socket.SocketServer;

public class SocketServerMain {

    public static void main(String[] args) {
        Hello helloService = new HelloServiceImpl();
        SocketServer socketRpcServer = new SocketServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registryService(rpcServiceConfig);
        socketRpcServer.start();
    }


}
