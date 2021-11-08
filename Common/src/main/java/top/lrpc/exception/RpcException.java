package top.lrpc.exception;

import top.lrpc.enums.RpcErrorMessageEnum;

public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
