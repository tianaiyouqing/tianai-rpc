package cloud.tianai.rpc.core.client.proxy.impl;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.Response;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.client.proxy.AbstractRpcProxy;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 12:01
 * @Description: JDK自带的RPC代理
 */
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
        // 通过负载均衡读取到对应的rpcClient
        RemotingClient rpcClient = loadBalance(request);
        CompletableFuture<Object> future = rpcClient.getchannel().request(request, super.requestTimeout);
        Object resObj = future.get(super.requestTimeout, TimeUnit.MILLISECONDS);
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
}
