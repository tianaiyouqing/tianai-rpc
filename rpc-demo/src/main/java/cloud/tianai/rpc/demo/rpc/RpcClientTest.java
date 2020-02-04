package cloud.tianai.rpc.demo.rpc;

import cloud.tianai.rpc.common.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.client.proxy.RpcProxy;
import cloud.tianai.rpc.core.client.proxy.impl.JdkRpcProxy;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class RpcClientTest {

    public static void main(String[] args) throws InterruptedException {
        // 编码解码器
        RpcClientConfiguration prop = new RpcClientConfiguration();
        prop.setCodec("hessian2");
        prop.setTimeout(5000);
        prop.setRequestTimeout(3000);
        prop.setProtocol("netty");

        URL nacosConf = new URL("nacos", "127.0.0.1", 8848);
        nacosConf = nacosConf.addParameter("namespace", "1ca3c65a-92a7-4a09-8de1-4bfe1c89d240");
        // 注册器
        prop.setRegistryUrl(nacosConf);
        // 注册器

        // 远程 客户端
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

        IntStream.range(0, 2000).parallel().forEach(i -> {
            String res = proxy.sayHello();
            System.out.println("返回消息:" + res);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
