package top.lrpc.impl;

import lombok.extern.slf4j.Slf4j;
import top.lrpc.Hello;
import top.lrpc.HelloE;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:52:00
 */
@Slf4j
public class HelloServiceImpl2 implements Hello {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(HelloE hello) {
        log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
