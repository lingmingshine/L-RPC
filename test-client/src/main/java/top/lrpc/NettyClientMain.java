package top.lrpc;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import top.lrpc.annotation.LScan;

@LScan(basePackage = {"top.lrpc"})
public class NettyClientMain {

    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
