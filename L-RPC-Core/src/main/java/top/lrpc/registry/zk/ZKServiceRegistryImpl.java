package top.lrpc.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import top.lrpc.registry.ServiceRegistry;
import top.lrpc.registry.util.CuratorUtils;

import java.net.InetSocketAddress;

public class ZKServiceRegistryImpl  implements ServiceRegistry {

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTRY_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPresistentNode(zkClient, servicePath);
    }
}
