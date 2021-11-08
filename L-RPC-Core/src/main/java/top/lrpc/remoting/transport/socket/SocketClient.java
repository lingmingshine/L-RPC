package top.lrpc.remoting.transport.socket;

import top.lrpc.registry.ServiceDiscovery;
import top.lrpc.remoting.transport.RpcRequestTransport;
import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.exception.RpcException;
import top.lrpc.extension.ExtensionLoader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient implements RpcRequestTransport {

    /**
     * 客户端：
     * 1.创建Socket对象并且连接指定的服务器地址（ip)和端口号（port)：
     * socket.connect(inetSocketAddress)
     * 2.连接建立之后，通过输出流向服务器发送请求信息
     * 3.通过输入流获取服务器响应信息
     * 4.关闭相关资源
     */

    private final ServiceDiscovery serviceDiscovery;

    public SocketClient() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    }

    @Override
    public Object sendRpcRequest(LRpcRequest lRpcRequest) {
        //服务发现--通过请求查询服务找到注册服务的服务器地址和端口
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(lRpcRequest);
        //通过socket连接
        try (Socket socket = new Socket()) {
            //socket建立连接
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //发送数据到服务器通过输出流
            objectOutputStream.writeObject(lRpcRequest);
            //读取回应信息到输入流
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败:", e);
        }
    }
}
