package top.lrpc.registry;

import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {

    /**
     * 查找rpcservice
     *
     * @param rpcRequest
     * @return
     */
    InetSocketAddress lookupService(LRpcRequest rpcRequest);


}
