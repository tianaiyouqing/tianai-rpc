package cloud.tianai.rpc.demo.rpc;

import cloud.tianai.rpc.core.client.proxy.AbstractRpcProxy;
import cloud.tianai.rpc.core.client.proxy.RpcProxy;
import cloud.tianai.rpc.core.client.proxy.impl.JdkRpcProxy;
import cloud.tianai.rpc.core.constant.RpcClientConfigConstant;

import java.util.Properties;

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

        // 远程 客户端
        prop.setProperty(RpcClientConfigConstant.PROTOCOL, "netty");
        prop.setProperty(RpcClientConfigConstant.WORKER_THREADS, String.valueOf(1));
        prop.setProperty(RpcClientConfigConstant.REQUEST_TIMEOUT, String.valueOf(3000));
        RpcProxy<Demo> rpcProxy = new JdkRpcProxy<>();
        Demo proxy = rpcProxy.createProxy(Demo.class, prop, true, true);
        for (int i = 0; i < 20; i++) {
            String res = proxy.sayHello();
            System.out.println("rpc调用返回数据:" + res);

        }
    }
}
