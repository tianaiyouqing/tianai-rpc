package cloud.tianai.rpc.core.client.proxy.impl;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.Response;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.client.proxy.AbstractRpcProxy;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 12:01
 * @Description: JDK自带的RPC代理
 */
@Slf4j
public class JdkRpcProxy<T> extends AbstractRpcProxy<T> implements InvocationHandler {

    @Override
    public T doCreateProxy() {
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{interfaceClass, Serializable.class}, this);
        return proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(proxy, args);
        }
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            if ("toString".equals(methodName)) {
                return proxy.toString();
            } else if ("hashCode".equals(methodName)) {
                return proxy.hashCode();
            }
        } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
            return proxy.equals(args[0]);
        }
        Request request = warpRequest(proxy, method, args);
        // 懒加载 registry
        startRegistryIfNecessary(super.prop);
        // 执行请求
        Object resObj = request(request, 0);
        Response response;
        if (resObj instanceof Response) {
            response = (Response) resObj;
        } else {
            response = new Response(request.getId());
            response.setResult(resObj);
            response.setStatus(Response.OK);
        }
        if (Response.OK == response.getStatus()) {
            // 如果是ok，直接返回
            return response.getResult();
        }
        // 直接抛异常
        throw new RpcException("rpc请求错误 ， status=" + response.getStatus() + "msg=" + response.getErrorMessage());
    }

    /**
     * 执行请求
     * @param request 请求体
     * @param currRetry 当前已重试次数
     * @return Object
     * @throws TimeoutException 重试次数达到后如果还请求不到，理应直接抛出异常
     */
    private Object request(Request request, int currRetry) throws TimeoutException {
        // 通过负载均衡读取到对应的rpcClient
        // 如果请求超时，理应再从负载均衡器里拿一个连接执行重试

        RemotingClient rpcClient = loadBalance(request);
        CompletableFuture<Object> future = rpcClient.getchannel().request(request, super.requestTimeout);
        Object resObj = null;
        try {
            resObj = future.get(super.requestTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            currRetry ++;
            // 如果超过重试次数， 直接抛异常
            if (currRetry > super.retry) {
                throw new TimeoutException("请求失败， 超过最大重试次数");
            }
            // 休眠100毫秒重试一下
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {
                // 不做处理
            }
            log.info("请求重试, 请求体 [{}], 当前已重试次数{}", request, currRetry);
            // 负责继续请求重试
            return request(request, currRetry);
        }
        return resObj;
    }
}
