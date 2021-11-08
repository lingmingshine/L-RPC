package top.lrpc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface LService {

    String version() default "";

    String group() default "";


}
