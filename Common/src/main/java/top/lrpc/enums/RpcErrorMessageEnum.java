package top.lrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  RpcErrorMessageEnum {

    CONNECT_SERVER_ERROR("客户端连接服务器失败"),
    SERVICE_INVOATION_ERROR("服务调用失败"),
    SERVICE_NOT_FOUND("未找到指定服务"),
    SERVICE_NOT_INTERFACE("注册服务没有实现接口"),
    REQUEST_NOT_MATCH("返回结果错误！请求和返回结果不匹配");

    private final String message;

}
