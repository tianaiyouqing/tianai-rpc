package cloud.tianai.rpc.demo.tokenverification;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.core.context.RpcContext;
import cloud.tianai.rpc.demo.rpc.Demo;
import cloud.tianai.rpc.demo.rpc.Demo2;
import cloud.tianai.rpc.demo.rpc.DemoImpl;
import cloud.tianai.rpc.demo.rpc.DemoImpl2;
import cloud.tianai.rpc.remoting.api.ChannelHolder;
import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.remoting.api.Response;
import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.remoting.api.util.ResponseUtils;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenVerificationServerTest {

    public static final Map<String, Object> tokenMap = new HashMap<>();
    public static final Map<SocketAddress, AtomicInteger> countMap = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        URL nacosConf = new URL("zookeeper", "127.0.0.1", 2181);
//            nacosConf = nacosConf.addParameter("namespace", "1ca3c65a-92a7-4a09-8de1-4bfe1c89d240");
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.addRpcInvocationPostProcessor(new RpcInvocationPostProcessor() {
            @Override
            public Response beforeInvoke(Method method, Request request, Object invokeObj) {
//                if ("login".equals(request.getMethodName())) {
//                    String username = (String) request.getRequestParam()[0];
//                    String password = (String) request.getRequestParam()[1];
//                    if (username.equals("123") && password.equals("456")) {
//                        String newToken = UUID.randomUUID().toString();
//                        Response response = ResponseUtils.warpResponse(newToken, request);
//                        tokenMap.put(newToken, "123");
//                        response.setStatus(Response.OK);
//                        return response;
//                    }else {
//                        Response response = new Response(request.getId());//ResponseUtils.warpResponse("用户名密码错误", request);
//                        response.setStatus((byte) 103);
//                        response.setErrorMessage("用户名密码错误");
//                        return response;
//                    }
//                }
                String token = String.valueOf(request.getHeader("token"));
                if (token == null || "null".equals(token)) {
                    // 没有token？ 那不好意思了, 直接关掉连接
                    Response response = new Response(request.getId());
//                    Response response = ResponseUtils.warpResponse("token为空", request);
                    response.setStatus(Response.SERVICE_NOT_SUPPORTED);
                    response.setErrorMessage("token为空");
                    return response;
                }
                if (!tokenMap.containsKey(token)) {

                    // token失效? 那不好意思, 告诉他token失效， 并且记录次数
                    SocketAddress remoteAddress = ChannelHolder.get().getRemoteAddress();
                    int count = countMap.computeIfAbsent(remoteAddress, k -> new AtomicInteger(0)).addAndGet(1);
//                    Response response = ResponseUtils.warpResponse("token失效", request);
                    Response response = new Response(request.getId());
                    response.setErrorMessage("token失效");
                    if (count > 5) {
                        response.setStatus(Response.SERVICE_NOT_SUPPORTED);
                    }else {
                        response.setStatus((byte) 101);
                    }
                    return response;
                }
                Map<String, Object> attachments = RpcContext.getRpcContext().getAttachments();
                System.out.println("token:" + token);
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
