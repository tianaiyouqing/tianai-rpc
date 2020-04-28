package cloud.tianai.rpc.demo.rpc;

import cloud.tianai.rpc.core.client.proxy.RpcProxyFactory;
import cloud.tianai.rpc.core.client.proxy.RpcProxyType;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.client.proxy.RpcProxy;
import cloud.tianai.rpc.core.client.proxy.impl.jdk.JdkRpcProxy;
import cloud.tianai.rpc.core.context.RpcContext;
import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.remoting.api.Response;
import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class RpcClientTest {

    public static void main(String[] args) throws InterruptedException {


//        RemotingDataProcessorEnhance.addInterceptor(new RemotingDataInterceptor() {
//            @Override
//            public Object beforeReadMessage(Channel channel, Object msg, Object extend) {
//                System.out.println("请求过来了===> " + msg);
//                return msg;
//            }
//
//            @Override
//            public Object beforeWriteMessage(Channel channel, Object msg, Object extend) {
//                // 写之前添加xid
//                if (msg instanceof Header) {
//                    ((Header) msg).setHeader("xid", "123123123123123");
//                }
//                System.out.println("写数据 ===>" + msg);
//                return msg;
//            }
//        });
        // 编码解码器
        RpcClientConfiguration prop = new RpcClientConfiguration();
        prop.setCodec("hessian2");
        prop.setTimeout(5000);
        prop.setRequestTimeout(3000);
        prop.setProtocol("netty");
        prop.setLoadBalance("random");
        prop.addRpcClientPostProcessor(new RpcClientPostProcessor() {

            @Override
            public void beforeRequest(Request request, RemotingClient remotingClient) {
            }

            @Override
            public void requestFinished(Request request, Response response, RemotingClient remotingClient) {

            }
        });
        URL nacosConf = new URL("zookeeper", "127.0.0.1", 2181);
//        nacosConf = nacosConf.addParameter("namespace", "1ca3c65a-92a7-4a09-8de1-4bfe1c89d240");
        // 注册器
        prop.setRegistryUrl(nacosConf);
        // 注册器

        // 远程 客户端
//        RpcProxy<Demo> rpcProxy = new JdkRpcProxy<>();
        Demo2 proxy = RpcProxyFactory.create(Demo2.class, prop, RpcProxyType.JDK_PROXY);
        // 添加附加数据
        RpcContext.getRpcContext().setAttachment("AAA", 123);
        System.out.println(proxy.helloRpc2());
//        proxy.toString();

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

//        RpcProxy<Demo2> rpcProxy2 = new JdkRpcProxy<>();
//        Demo2 proxy2 = rpcProxy2.createProxy(Demo2.class, prop);

//        Demo2 proxy2 = RpcProxyFactory.create(Demo2.class, prop, RpcProxyType.JAVASSIST_PROXY);
//        proxy2.toString();
//        proxy2.hashCode();
//        IntStream.range(0, 5).forEach(i -> {
//            String res = proxy.sayHello();
//            System.out.println("返回消息:" + res);
//            try {
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//

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
