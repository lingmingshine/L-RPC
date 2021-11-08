package top.lrpc.remoting.transport.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class LRpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;


    private String requestId;

    private String interfaceName;

    private String methodName;

    private String group;

    private String version;

    private Object[] parameters;
    private Class<?>[] paramTypes;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }

}
