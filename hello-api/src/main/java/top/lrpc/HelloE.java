package top.lrpc;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class HelloE implements Serializable {

    private String message;
    private String description;
}
