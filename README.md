# TIANAI-RPC
- 这是一个rpc框架的实现
- 本人闲暇之余写的RPC框架，供学习使用，(线上还是用成熟的RPC框架比较好，比如 dubbo,grpc等)
---
> 该RPC框架目前实现有
- 基于zookeeper的服务注册
- 基于Netty实现的远程通讯
- 基于hessian2实现的序列化、反序列化
- 实现了基于轮询策略的负载均衡
- 使用Netty自带的心跳机制实现心跳机制
- 实现网络抖动后造成的链接断开后重连
> 未实现的有
- 暂未实现zk的持久节点的定时清除

> 实现调用demo
- server端
```java
public class RpcServerImplTest2 {

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.codec("hessian2")
                    .timeout(5000)
                    // 设置服务注册为zookeeper， 支持zookeeper和nacos两个服务注册
                    .registry(new URL("zookeeper", "127.0.0.1", 2181))
                    .server("netty")
                    .port(20881)
                    .start();
            // 注册
            serverBootstrap.register(Demo.class, new DemoImpl());
            System.out.println("启动成功");
        }).start();

        TimeUnit.HOURS.sleep(1);
    }
}
```


- 客户端
```java
public class RpcClientTest {

    public static void main(String[] args) {
        RpcClientConfiguration prop = new RpcClientConfiguration();
        // 序列化，默认是hessian2
        prop.setCodec("hessian2");
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
        RpcProxy<Demo> rpcProxy = new JdkRpcProxy<>();
        Demo proxy = rpcProxy.createProxy(Demo.class, prop, true, true);
        for (int i = 0; i < 20; i++) {
            // 执行RPC请求
            String res = proxy.sayHello();
            System.out.println("rpc调用返回数据:" + res);
        }
    }
}

```