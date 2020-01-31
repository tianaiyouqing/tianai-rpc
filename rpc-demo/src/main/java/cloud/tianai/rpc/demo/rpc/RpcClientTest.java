package cloud.tianai.rpc.demo.rpc;

import cloud.tianai.rpc.core.client.proxy.RpcProxy;
import cloud.tianai.rpc.core.client.proxy.impl.JdkRpcProxy;
import cloud.tianai.rpc.core.constant.RpcClientConfigConstant;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class RpcClientTest {

    public static void main(String[] args) throws InterruptedException {
        Properties prop = new Properties();
        // 编码解码器
        prop.setProperty(RpcClientConfigConstant.CODEC, "hessian2");
        prop.setProperty(RpcClientConfigConstant.TIMEOUT, String.valueOf(5000));

        // 注册器
        prop.setProperty(RpcClientConfigConstant.REGISTER, "zookeeper");
        prop.setProperty(RpcClientConfigConstant.REGISTRY_HOST, "192.168.1.6");
        prop.setProperty(RpcClientConfigConstant.REGISTRY_PORT, String.valueOf(2181));

        // 远程 客户端
        prop.setProperty(RpcClientConfigConstant.PROTOCOL, "netty");
        prop.setProperty(RpcClientConfigConstant.REQUEST_TIMEOUT, String.valueOf(3000));
        prop.setProperty(RpcClientConfigConstant.TIMEOUT, String.valueOf(3000));
        RpcProxy<Demo> rpcProxy = new JdkRpcProxy<>();
        Demo proxy = rpcProxy.createProxy(Demo.class, prop, true, true);
//        for (int i1 = 0; i1 < 1000; i1++) {
//            new Thread(() -> {
//                for (int i2 = 0; i2 < 20; i2++) {
//                    if (new Random().nextInt() % 2 ==0) {
//                        String res = proxy.sayHello();
//                        System.out.println("rpc调用返回数据:" + res);
//                    }else{
//                        DemoRes demoRes = proxy.helloRpc();
//                        System.out.println("rpc调用返回:" + demoRes);
//                    }
//                }
//            }).start();
//        }

        RpcProxy<Demo2> rpcProxy2 = new JdkRpcProxy<>();
        Demo2 proxy2 = rpcProxy2.createProxy(Demo2.class, prop, true, true);

        IntStream.range(0,2000).parallel().forEach(i -> {
            String res =  proxy.sayHello();
//            System.out.println("rpc调用返回:" + res);
        });


//        for (int i = 0; i < 1000; i++) {
//            new Thread(() -> {
//                for (int i1 = 0; i1 < 20; i1++) {
//                    String res = proxy.sayHello();
//                    System.out.println("rpc调用返回:" + res);
//                }
//            }).start();
//        }
//
        TimeUnit.HOURS.sleep(1);
    }
}
