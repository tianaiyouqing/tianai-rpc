package cloud.tianai.rpc.core.client.proxy;

import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 12:01
 * @Description: RPC代理
 */
public interface RpcProxy<T> {

    /**
     * 创建代理
     * @param interfaceClass 接口class
     * @param conf 配置
     * @return
     */
    T createProxy(Class<T> interfaceClass, RpcClientConfiguration conf);
}
