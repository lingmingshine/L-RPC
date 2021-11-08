package top.lrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@ToString
public enum RpcResponseEnum {

    SUCCESS(200, "远程调用成功"),
    FAIL(500, "远程调用失败");

    private final int code;

    private final String message;

}
