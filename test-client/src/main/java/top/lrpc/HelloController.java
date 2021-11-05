package top.lrpc;

import org.springframework.stereotype.Component;
import top.lrpc.annotation.LReference;

@Component
public class HelloController {


    @LReference(version = "version1", group = "test1")
    private Hello hello1;


    public void test() throws InterruptedException {
        String hello = hello1.hello(new HelloE("111","222"));
        assert "Hello description is shezhicuowum".equals(hello);
        Thread.sleep(12000);
        for (int i = 0; i < 10; i++) {
            System.out.println(hello1.hello(new HelloE("111", "222")));
        }

    }

}
