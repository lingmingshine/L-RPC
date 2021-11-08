package top.lrpc.remoting.transport.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import top.lrpc.compress.Compress;
import top.lrpc.enums.CompressTypeEnum;
import top.lrpc.enums.SerializationTypeEnum;
import top.lrpc.extension.ExtensionLoader;
import top.lrpc.remoting.transport.dto.LRpcMessage;
import top.lrpc.remoting.transport.netty.client.constants.RpcConstants;
import top.lrpc.serializer.Serializer;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LRpcMessageEncoder extends MessageToByteEncoder<LRpcMessage> {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LRpcMessage lRpcMessage, ByteBuf byteBuf) throws Exception {
        try {
            byteBuf.writeBytes(RpcConstants.MAGIC_NUMBER);
            byteBuf.writeByte(RpcConstants.VERSION);
            byteBuf.writeByte(byteBuf.writerIndex() + 4);
            byte messageType = lRpcMessage.getMessageType();
            byteBuf.writeByte(messageType);
            byteBuf.writeByte(lRpcMessage.getCodec());
            byteBuf.writeByte(CompressTypeEnum.GZIP.getCode());
            byteBuf.writeInt(ATOMIC_INTEGER.getAndIncrement());
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                String codecName = SerializationTypeEnum.getName(lRpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(lRpcMessage.getData());

                String compressName = CompressTypeEnum.getName(lRpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }
            if (bodyBytes != null) {
                byteBuf.writeBytes(bodyBytes);
            }
            int writeIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            byteBuf.writeInt(fullLength);
            byteBuf.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("加密失败！", e);
        }
    }
}
