package top.lrpc.spring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import top.lrpc.annotation.LReference;
import top.lrpc.annotation.LService;
import top.lrpc.config.RpcServiceConfig;
import top.lrpc.extension.ExtensionLoader;
import top.lrpc.factory.SingletionFactory;
import top.lrpc.provider.ServerProvider;
import top.lrpc.provider.impl.ZKServiceProviderImpl;
import top.lrpc.proxy.RpcClientProxy;
import top.lrpc.remoting.transport.RpcRequestTransport;

import java.lang.reflect.Field;

@Component
@Slf4j
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServerProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletionFactory.getInstance(ZKServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(LService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), LService.class.getCanonicalName());
            LService service = bean.getClass().getAnnotation(LService.class);
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(service.group())
                    .version(service.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            LReference rpcReference = declaredField.getAnnotation(LReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
