package cloud.tianai.rpc.demo.api;

import cloud.tianai.remoting.api.RemotingChannelHolder;
import cloud.tianai.remoting.api.Request;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TestProxy implements InvocationHandler {
    private RemotingChannelHolder remotingChannelHolder;
    private Class<?> interfaceClass;
    public  <T> T newProxy(Class<T> interfaceClass, RemotingChannelHolder channelHolder) {
        this.remotingChannelHolder = channelHolder;
        this.interfaceClass = interfaceClass;
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{interfaceClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 包装成Handler
        Request request = new Request();
        request.setVersion("v1");
        request.setRequestParam(args);
        request.setMethodName(method.getName());
        request.setInterfaceType(interfaceClass);
        request.setReturnType(method.getReturnType());
        request.setHeartbeat(false);
        CompletableFuture<Object> future = remotingChannelHolder.request(request, 3000);
        Object result = future.get(10, TimeUnit.SECONDS);

        return result;
    }
}
