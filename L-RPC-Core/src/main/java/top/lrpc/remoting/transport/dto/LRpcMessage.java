package top.lrpc.remoting.transport.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LRpcMessage {

    //消息类型
    private byte messageType;
    //序列化类型
    private byte codec;
    //压缩类型
    private byte compress;
    //请求id
    private int requestId;

    private Object data;
}
