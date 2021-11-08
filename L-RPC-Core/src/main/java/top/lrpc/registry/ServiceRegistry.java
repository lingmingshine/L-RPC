package top.lrpc.registry;

import top.lrpc.extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {

    /**
     * 注册服务
     * @param rpcServiceName
     * @param inetSocketAddress
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
