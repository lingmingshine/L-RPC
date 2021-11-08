package top.lrpc.provider.impl;

import lombok.extern.slf4j.Slf4j;
import top.lrpc.registry.ServiceRegistry;
import top.lrpc.config.RpcServiceConfig;
import top.lrpc.enums.RpcErrorMessageEnum;
import top.lrpc.exception.RpcException;
import top.lrpc.extension.ExtensionLoader;
import top.lrpc.provider.ServerProvider;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZKServiceProviderImpl implements ServerProvider {

    private final Map<String,Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;


    public ZKServiceProviderImpl(){
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");

    }

    /**
     * 添加服务
     * @param rpcServiceConfig
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("添加service: {} 和接口:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if(null == service){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try{
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(),new InetSocketAddress(host,9998));
        }catch (UnknownHostException e){
            log.error("获取地址时候出现错误！");
        }

    }
}
