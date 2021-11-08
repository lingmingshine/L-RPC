package top.lrpc.loadbanlance;

import top.lrpc.remoting.transport.dto.LRpcRequest;

import java.util.List;

public abstract class AbstractLoadBanlance implements LoadBanlance {

    @Override
    public String selectServiceAddress(List<String> serviceAddress, LRpcRequest rpcRequest) {
        //通过负载均衡选择服务地址
        if(serviceAddress == null || serviceAddress.size() == 0){
            return null;
        }
        //只有一个情况下直接返回就行
        if(serviceAddress.size() == 1){
            return serviceAddress.get(0);
        }
        return doSelect(serviceAddress,rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddress, LRpcRequest rpcRequest);
}
