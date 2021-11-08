package top.lrpc.remoting.transport.netty.client;

import top.lrpc.remoting.transport.dto.LRpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类的作用就是手动结束future，防止一直阻塞等待
 *
 */

public class FliterRequest {

    private static final Map<String, CompletableFuture<LRpcResponse<Object>>> FILTER_REQUEST_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<LRpcResponse<Object>> future) {
        FILTER_REQUEST_FUTURES.put(requestId, future);
    }

    public void complete(LRpcResponse<Object> rpcResponse) {
        CompletableFuture<LRpcResponse<Object>> future = FILTER_REQUEST_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
