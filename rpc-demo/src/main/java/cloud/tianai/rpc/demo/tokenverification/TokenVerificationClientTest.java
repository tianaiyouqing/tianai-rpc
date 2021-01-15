package cloud.tianai.rpc.demo.tokenverification;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.core.client.proxy.RpcProxyFactory;
import cloud.tianai.rpc.core.client.proxy.RpcProxyType;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.core.context.RpcContext;
import cloud.tianai.rpc.demo.rpc.Demo;
import cloud.tianai.rpc.demo.rpc.Demo2;
import cloud.tianai.rpc.demo.rpc.DemoImpl;
import cloud.tianai.rpc.demo.rpc.DemoImpl2;
import cloud.tianai.rpc.remoting.api.*;
import cloud.tianai.rpc.remoting.api.util.ResponseUtils;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenVerificationClientTest {

    public static final Map<String, Object> tokenMap = new HashMap<>();
    public static final Map<SocketAddress, AtomicInteger> countMap = new HashMap<>();

    public static String token = null;
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
                request.setHeader("token", token);
            }

            @Override
            public void requestFinished(Request request, Response response, RemotingClient remotingClient) {

            }
        });
        URL nacosConf = new URL("zookeeper", "127.0.0.1", 2181);
        // 注册器
        prop.setRegistryUrl(nacosConf);
        // 注册器

        // 远程 客户端
        Demo2 proxy = RpcProxyFactory.create(Demo2.class, prop, RpcProxyType.JDK_PROXY);
        // 添加附加数据
        RpcContext.getRpcContext().setAttachment("AAA", 123);
        try {
            token = proxy.login("123", "456");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("token:" + token);
        System.out.println(proxy.helloRpc2());
//        System.out.println(proxy.helloRpc2());
//        System.out.println(proxy.helloRpc2());

//
        TimeUnit.HOURS.sleep(1);
    }
}
