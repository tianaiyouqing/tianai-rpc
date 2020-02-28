package cloud.tianai.rpc.core.client.proxy;

import cloud.tianai.rpc.core.client.proxy.impl.javassist.JavassistRpcProxy;
import cloud.tianai.rpc.core.client.proxy.impl.jdk.JdkRpcProxy;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/28 23:29
 * @Description: RPC工厂
 */
public class RpcProxyFactory {

    public static <T> T create(Class<T> interfaceClass, RpcClientConfiguration prop, RpcProxyType type) {
        RpcProxy<T> rpcProxy = getRpcProxy(interfaceClass, type);
        return rpcProxy.createProxy(interfaceClass, prop);
    }

    private static <T> RpcProxy<T> getRpcProxy(Class<T> clazz, RpcProxyType type) {
        switch (type) {
            case JDK_PROXY:
                return new JdkRpcProxy<T>();
            case JAVASSIST_PROXY:
                return new JavassistRpcProxy<T>();
            default:
                return new JavassistRpcProxy<T>();
        }
    }


}
