# TIANAI-RPC
- 这是一个rpc框架的实现
- 本人闲暇之余写的RPC框架，供学习使用，(线上还是用成熟的RPC框架比较好，比如 dubbo,grpc等)
---
> 该RPC框架目前实现有
- 基于zookeeper的服务注册
- 基于Netty实现的远程通讯
- 基于hessian2实现的序列化、反序列化
- 实现了基于轮询策略的负载均衡

> 未实现的有
- 暂未实现心跳机制
- 暂未实现网络抖动后造成的链接断开后重连
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
        Properties prop = new Properties();
        // 编码解码器
        prop.setProperty(RpcClientConfigConstant.CODEC, "hessian2");
        prop.setProperty(RpcClientConfigConstant.TIMEOUT, String.valueOf(5000));

        // 注册器
        prop.setProperty(RpcClientConfigConstant.REGISTER, "zookeeper");
        prop.setProperty(RpcClientConfigConstant.REGISTRY_HOST, "192.168.1.6");
        prop.setProperty(RpcClientConfigConstant.REGISTRY_PORT, String.valueOf(2181));

        // 远程客户端，默认netty
        prop.setProperty(RpcClientConfigConstant.PROTOCOL, "netty");
        // 工作线程，默认cpu核心数+1
        prop.setProperty(RpcClientConfigConstant.WORKER_THREADS, String.valueOf(1));
        // 请求超时时间
        prop.setProperty(RpcClientConfigConstant.REQUEST_TIMEOUT, String.valueOf(3000));
        
        // 创建RPC代理
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