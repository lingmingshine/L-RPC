package top.lrpc.provider;

import top.lrpc.config.RpcServiceConfig;

public interface ServerProvider {


    /**
     *添加service
     * @param rpcServiceConfig
     */
    void addService(RpcServiceConfig  rpcServiceConfig);

    /**
     * 获取service
     * @param rpcServiceName
     * @return
     */
    Object getService(String rpcServiceName);


    /**
     * 推送service
     * @param rpcServiceConfig
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}
