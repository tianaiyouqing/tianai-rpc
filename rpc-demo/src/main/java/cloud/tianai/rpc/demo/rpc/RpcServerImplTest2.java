package cloud.tianai.rpc.demo.rpc;


import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.core.configuration.RpcServerConfiguration;
import cloud.tianai.rpc.core.context.RpcContext;
import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.remoting.api.Response;
import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcServerImplTest2 {

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        URL nacosConf = new URL("zookeeper", "127.0.0.1", 2181);
//            nacosConf = nacosConf.addParameter("namespace", "1ca3c65a-92a7-4a09-8de1-4bfe1c89d240");
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.addRpcInvocationPostProcessor(new RpcInvocationPostProcessor() {
            @Override
            public Response beforeInvoke(Method method, Request request, Object invokeObj) {
                Map<String, Object> attachments = RpcContext.getRpcContext().getAttachments();
                System.out.println("执行请求前: method:" + method +", req:" + request +",ref:" + invokeObj + ",attachments=" + attachments);
                return null;
            }

            @Override
            public Response invokeFinished(Request request, Response response, Method method, Object invokeObj) {
                System.out.println("执行请求后: method:" + method +", req:" + request +",ref:" + invokeObj +", res:" + response);
                return response;
            }

            @Override
            public Response invokeError(Request request, Response response, Method method, Object invokeObj, Throwable ex) {
                System.out.println("执行请求后 异常: method:" + method +", req:" + request +",ref:" + invokeObj +", res:" + response +",ex:" + ex);
                return response;
            }
        });
        serverBootstrap.codec("hessian2")
        .timeout(5000)
        .registry(nacosConf)
        .protocol("netty")
        .port(20881);

        serverBootstrap
//                .register(Demo.class, new DemoImpl())
//                .register(Demo2.class, new DemoImpl2())
                .start();

        long end1 = System.currentTimeMillis();

        serverBootstrap.register(Demo.class, new DemoImpl(), Collections.singletonMap(CommonConstant.WEIGHT_KEY, 100))
                .register(Demo2.class, new DemoImpl2(), Collections.singletonMap("aaa", "ccc"));

        long end = System.currentTimeMillis();
        // 注册
        System.out.println("启动成功, 总耗时:" + (end - start) + ", 启动耗时:" + (end1  - start));


        TimeUnit.HOURS.sleep(1);
    }
}