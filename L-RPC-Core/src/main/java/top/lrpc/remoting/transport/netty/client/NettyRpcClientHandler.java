package top.lrpc.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import top.lrpc.enums.CompressTypeEnum;
import top.lrpc.enums.SerializationTypeEnum;
import top.lrpc.remoting.transport.dto.LRpcMessage;
import top.lrpc.remoting.transport.dto.LRpcResponse;
import top.lrpc.remoting.transport.netty.client.constants.RpcConstants;
import top.lrpc.factory.SingletionFactory;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 心跳检测机制：
     * 定时发送一个自定义的结构体（心跳包，心跳帧），让对方知道自己"在线"，确保连接的有效性
     * 心跳检测规定定时发送心跳检测数据包，接送方接受心跳包回复，否则认为连接断开
     */


    private final FliterRequest fliterRequest;

    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.fliterRequest = SingletionFactory.getInstance(FliterRequest.class);
        this.nettyRpcClient = SingletionFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 重写方法
     * 在方法中处理idleEvent.state() == IdleState.WRITER_IDLE
     * @param ctx
     * @param evt
     * @throws Exception
     */

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {//超时事件
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {//写
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                //客户端继续写入心跳包进行channel传输
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                LRpcMessage rpcMessage = new LRpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("客户端获取信息：[{}]", msg);
            if (msg instanceof LRpcMessage) {
                LRpcMessage message = ((LRpcMessage) msg);
                byte messageType = message.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    log.info("heart:[{}]", message.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    LRpcResponse<Object> rpcResponse = ((LRpcResponse<Object>) message.getData());
                    fliterRequest.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
