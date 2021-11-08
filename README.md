# L-RPC
L-RPC：轻量级远程调用框架

##### 核心代码框架包：

> ![1636335053853](/png/1.png)

```xml
loadbanlance:负载平衡
proxy:动态代理
registry：注册服务
remoting.transport:socket和netty两种方式实现远程调用通信
serializer:序列化
spring:依托spring的自定义注解实现
主要是通过remoting.transport包下面两种socket和netty实现方式，进行辐射全部的代码
```

#### L-RPC框架目前实现的功能：

1.分别使用socket和nio形式的netty框架连接实现通信连接

> socket:使用输入输出流对信息处理，是阻塞模式的，服务端在等待客户端消息
>
> netty:~~

2.使用zookeeper的CuratorFramework实现服务注册与发现

3.通过@SPI注解以及springboot自动化配置，实现第三方接口代码实现

4.引用参照dubbo的负载均衡策略：一致性哈希算法实现

-----待完善
