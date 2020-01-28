package cloud.tianai.rpc.demo.rpc;

import cloud.tianai.rpc.core.client.proxy.RpcProxy;
import cloud.tianai.rpc.core.client.proxy.impl.JdkRpcProxy;
import cloud.tianai.rpc.core.constant.RpcClientConfigConstant;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
        prop.setProperty(RpcClientConfigConstant.REQUEST_TIMEOUT, String.valueOf(30000));
        prop.setProperty(RpcClientConfigConstant.TIMEOUT, String.valueOf(30000));
        RpcProxy<Demo> rpcProxy = new JdkRpcProxy<>();
        Demo proxy = rpcProxy.createProxy(Demo.class, prop, true, true);
        for (int i1 = 0; i1 < 1000; i1++) {
            new Thread(() -> {
                for (int i2 = 0; i2 < 20; i2++) {
                    String res = proxy.sayHello();
                    System.out.println("rpc调用返回数据:" + res);
                }
            }).start();
        }

        TimeUnit.HOURS.sleep(1);
    }
}
