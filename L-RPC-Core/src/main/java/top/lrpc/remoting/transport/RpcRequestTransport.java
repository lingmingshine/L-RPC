package top.lrpc.remoting.transport;

import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.extension.SPI;

@SPI
public interface RpcRequestTransport {

    /**
     * 发送请求
     * @param lRpcRequest
     * @return
     */
    Object sendRpcRequest(LRpcRequest lRpcRequest);
}
