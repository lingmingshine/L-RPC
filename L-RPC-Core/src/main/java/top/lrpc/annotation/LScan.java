package top.lrpc.annotation;

import org.springframework.context.annotation.Import;
import top.lrpc.spring.CustomScannerRegistry;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Import(CustomScannerRegistry.class)
public @interface LScan {

    String[] basePackage();
}
