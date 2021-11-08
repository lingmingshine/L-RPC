package top.lrpc.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import top.lrpc.registry.ServiceDiscovery;
import top.lrpc.registry.util.CuratorUtils;
import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.enums.RpcErrorMessageEnum;
import top.lrpc.exception.RpcException;
import top.lrpc.extension.ExtensionLoader;
import top.lrpc.loadbanlance.LoadBanlance;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZKServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBanlance loadBanlance;

    //必须是无参构造器，因为初始化的时候会先初始化对象，刚才loadBanlance是有参构造器所以创建实例失败
    public ZKServiceDiscoveryImpl() {
        this.loadBanlance = ExtensionLoader.getExtensionLoader(LoadBanlance.class).getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(LRpcRequest rpcRequest) {
        //获取rpcservice名称
        String rpcServiceName = rpcRequest.getRpcServiceName();
        //在注册中心获取客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //获取该路径下的服务list
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serviceUrlList == null || serviceUrlList.size() == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND, rpcServiceName);
        }
        //负载均衡策略
        String targetServiceUrl = loadBanlance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("成功找到服务地址：[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
