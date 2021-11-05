package top.lrpc.impl;

import lombok.extern.slf4j.Slf4j;
import top.lrpc.Hello;
import top.lrpc.HelloE;
import top.lrpc.annotation.LService;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:52:00
 */
@Slf4j
@LService(group = "test1", version = "version1")
public class HelloServiceImpl implements Hello {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(HelloE hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
