package top.lrpc.handler;

import lombok.extern.slf4j.Slf4j;
import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.exception.RpcException;
import top.lrpc.factory.SingletionFactory;
import top.lrpc.provider.ServerProvider;
import top.lrpc.provider.impl.ZKServiceProviderImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RPC请求处理类
 */

@Slf4j
public class RpcRequestHandler {

    private final ServerProvider serverProvider;

    public RpcRequestHandler() {
        serverProvider = SingletionFactory.getInstance(ZKServiceProviderImpl.class);
    }


    public Object handle(LRpcRequest lRpcRequest) {
        //通过rpc请求获取服务名称，再得到服务对象
        Object service = serverProvider.getService(lRpcRequest.getRpcServiceName());
        //再进行动态代理获取对象
        return invokeTargetMethod(lRpcRequest, service);
    }

    private Object invokeTargetMethod(LRpcRequest lRpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(lRpcRequest.getMethodName(), lRpcRequest.getParamTypes());
            result = method.invoke(service, lRpcRequest.getParameters());
            log.info("服务[{}],成功获取到方法[{}]",lRpcRequest.getInterfaceName(),lRpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(),e);
        }
        return result;
    }
}
