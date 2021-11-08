package top.lrpc.remoting.transport.dto;

import lombok.*;
import top.lrpc.enums.RpcResponseEnum;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LRpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    private String requestId;
    private Integer code;
    private String message;

    private T data;

    public static <T> LRpcResponse<T> success(T data, String requestId) {
        LRpcResponse<T> lRpcResponse = new LRpcResponse<>();
        lRpcResponse.setCode(RpcResponseEnum.SUCCESS.getCode());
        lRpcResponse.setMessage(RpcResponseEnum.SUCCESS.getMessage());
        lRpcResponse.setRequestId(requestId);
        if (null != data) {
            lRpcResponse.setData(data);
        }
        return lRpcResponse;
    }

    public static <T> LRpcResponse<T> fail(RpcResponseEnum rpcResponseEnum) {
        LRpcResponse<T> response = new LRpcResponse<>();
        response.setCode(rpcResponseEnum.getCode());
        response.setMessage(rpcResponseEnum.getMessage());
        return response;
    }

}
