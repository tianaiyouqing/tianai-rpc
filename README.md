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
public class RpcServerImplTest {

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
       new Thread(() -> {
           RpcServer rpcServer = new RpcServerImpl();
           Properties prop = new Properties();

           // 编码解码器, 默认使用 hessian2
           prop.setProperty(RpcServerConfigConstant.CODEC, "hessian2");
          

           // 注册器 默认使用 zookeeper
           prop.setProperty(RpcServerConfigConstant.REGISTER, "zookeeper");
           prop.setProperty(RpcServerConfigConstant.REGISTRY_HOST, "127.0.0.1");
           prop.setProperty(RpcServerConfigConstant.REGISTRY_PORT, String.valueOf(2181));

           // 远程 server 默认使用netty
           prop.setProperty(RpcServerConfigConstant.PROTOCOL, "netty");
           // ip
           prop.setProperty(RpcServerConfigConstant.HOST, "192.168.1.6");
           // 端口
           prop.setProperty(RpcServerConfigConstant.PORT, String.valueOf(20885));
           // 工作线程数， 默认是cpu核心数+1
           prop.setProperty(RpcServerConfigConstant.WORKER_THREADS, String.valueOf(8));
           // boss线程数，默认1
           prop.setProperty(RpcServerConfigConstant.BOSS_THREADS, String.valueOf(1));
           // 超时 
           prop.setProperty(RpcServerConfigConstant.TIMEOUT, String.valueOf(5000));
           
            // 启动RPCServer
           rpcServer.start(prop);
            
           // 注册一个实例进去
           rpcServer.register(Demo.class, new DemoImpl());
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

        // 远程 server
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