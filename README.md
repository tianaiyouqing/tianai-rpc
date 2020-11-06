# TIANAI-RPC
- 这是一个rpc框架的实现
- 本人闲暇之余写的RPC框架，供学习使用，(线上还是用成熟的RPC框架比较好，比如 dubbo,grpc等)
- 该框架已在本人项目中测试，性能卓越，且相比 dubbo 框架 代码更加轻量级，阅读学习rpc框架的话，
  阅读该框架源码那是不二之选(代码中有些部分参考了dubbo)
---
> 该RPC框架目前实现有
- 基于zookeeper 和 nacos 的服务注册, 
- 基于Netty实现的远程通讯
- 基于hessian2 和 protostuff 实现的序列化、反序列化
- 实现了基于轮询策略 和 随机权重策略 的负载均衡
- 使用Netty自带的心跳机制实现心跳机制
- 实现网络抖动后造成的链接断开后重连
- 增加拦截器功能，自定义逻辑以及整合其他框架更加方便
- 基于SPI的模块化管理，更加方便扩展模块

> 实现调用demo
- server端
```java
public class RpcServerImplTest2 {

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.codec("protostuff")
                .timeout(5000)
                // 设置服务注册为zookeeper， 支持zookeeper和nacos两个服务注册
                .registry(new URL("zookeeper", "127.0.0.1", 2181))
                .server("netty")
                .port(20881)
                .start();
        // 注册
        serverBootstrap.register(Demo.class, new DemoImpl());
        System.out.println("启动成功");
    }
}
```


- 客户端
```java
public class RpcClientTest {

    public static void main(String[] args) {
        RpcClientConfiguration prop = new RpcClientConfiguration();
        // 序列化，默认是protostuff
        prop.setCodec("protostuff");
        // 超时，默认是5000
        prop.setTimeout(5000);
        // 请求超时，默认是3000
        prop.setRequestTimeout(3000);
        // 设置客户端为netty， 默认是netty
        prop.setProtocol("netty");
        
        // 服务注册，目前支持 zookeeper和nacos两个， 默认是zookeeper 
        // 设置服务注册 ， 为nacos
        // URL nacosConf = new URL("nacos", "127.0.0.1", 8848);
        // nacosConf = nacosConf.addParameter("namespace", "1ca3c65a-92a7-4a09-8de1-4bfe1c89d240");
        
        // 设置服务注册 为zookeeper
        prop.setRegistryUrl(new URL("zookeeper", "127.0.0.1", 2181));
        // 注册器

        // 远程 客户端
        Demo proxy = RpcProxyFactory.create(Demo.class, prop, RpcProxyType.JDK_PROXY);
        for (int i = 0; i < 20; i++) {
            // 执行RPC请求
            String res = proxy.sayHello();
            System.out.println("rpc调用返回数据:" + res);
        }
    }
}

```
- qq群: 1021884609