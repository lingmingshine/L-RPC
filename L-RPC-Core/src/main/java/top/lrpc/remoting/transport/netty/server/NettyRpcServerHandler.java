package top.lrpc.remoting.transport.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import top.lrpc.enums.CompressTypeEnum;
import top.lrpc.enums.RpcResponseEnum;
import top.lrpc.enums.SerializationTypeEnum;
import top.lrpc.factory.SingletionFactory;
import top.lrpc.handler.RpcRequestHandler;
import top.lrpc.remoting.transport.dto.LRpcMessage;
import top.lrpc.remoting.transport.dto.LRpcRequest;
import top.lrpc.remoting.transport.dto.LRpcResponse;
import top.lrpc.remoting.transport.netty.client.constants.RpcConstants;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {


    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletionFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            if(msg instanceof LRpcMessage){
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((LRpcMessage) msg).getMessageType();
                LRpcMessage rpcMessage = new LRpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                //判断如果是心跳包的话，不需要处理消息，只要回复心跳
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    LRpcRequest rpcRequest = (LRpcRequest) ((LRpcMessage) msg).getData();
                    //动态代理获取rpc对象
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        LRpcResponse<Object> rpcResponse = LRpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        LRpcResponse<Object> rpcResponse = LRpcResponse.fail(RpcResponseEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("channel不可写入，消息丢失");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {//超时事件
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {//读操作
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
