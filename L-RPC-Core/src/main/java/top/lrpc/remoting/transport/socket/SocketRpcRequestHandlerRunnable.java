package top.lrpc.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.remoting.transport.dto.LRpcResponse;
import top.lrpc.factory.SingletionFactory;
import top.lrpc.handler.RpcRequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 多线程处理客户端的详细Runnable类
 */

@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable {

    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;


    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = SingletionFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("服务处理消息来自客户端线程[{}]", Thread.currentThread().getName());
        OutputStream out;

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            //读取输入流的rpc请求信息
            LRpcRequest lRpcRequest = ((LRpcRequest) objectInputStream.readObject());
            //通过代理获取到需要请求对象，比如service对象
            Object result = rpcRequestHandler.handle(lRpcRequest);
            //返回结果到输出流
            objectOutputStream.writeObject(LRpcResponse.success(result, lRpcRequest.getRequestId()));
            //刷新缓存
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("出现异常:", e);
        }
    }
}
