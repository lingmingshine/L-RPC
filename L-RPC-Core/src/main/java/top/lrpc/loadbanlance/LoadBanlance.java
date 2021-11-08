package top.lrpc.loadbanlance;

import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.extension.SPI;

import java.util.List;

@SPI
public interface LoadBanlance {

    String selectServiceAddress(List<String> serviceAddress, LRpcRequest rpcRequest);
}
