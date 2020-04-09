package cloud.tianai.rpc.core.client.proxy.impl.jdk;

import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.remoting.api.Response;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.client.proxy.AbstractRpcProxy;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            if (TO_STRING_FUN_NAME.equals(methodName)) {
                return super.toString();
            } else if (HASH_CODE_FUN_NAME.equals(methodName)) {
                return super.hashCode();
            }
        } else if (parameterTypes.length == 1 && EQUALS_FUN_NAME.equals(methodName)) {
            return proxy.equals(args[0]);
        }
        Request request = warpRequest(proxy, method, args);
        // 执行请求
        Response response = rpcClientTemplate.request(request, requestTimeout, retry);
        if (Response.OK == response.getStatus()) {
            // 如果是ok，直接返回
            return response.getResult();
        }
        // 直接抛异常
        throw new RpcException("rpc请求错误 ， status=" + response.getStatus() + "msg=" + response.getErrorMessage());
    }
}
