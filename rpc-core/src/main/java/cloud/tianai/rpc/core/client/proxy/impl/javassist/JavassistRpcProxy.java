package cloud.tianai.rpc.core.client.proxy.impl.javassist;

import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.Response;
import cloud.tianai.rpc.common.bytecode.Proxy;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.client.proxy.AbstractRpcProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/28 23:12
 * @Description: 字节码代理
 */
public class JavassistRpcProxy<T> extends AbstractRpcProxy<T> implements InvocationHandler {
    @Override
    protected T doCreateProxy() {
        //noinspection unchecked
        return (T) Proxy.getProxy(interfaceClass).newInstance(this);
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
