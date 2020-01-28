package cloud.tianai.rpc.core.client.proxy;

import java.util.Properties;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 12:01
 * @Description: RPC代理
 */
public interface RpcProxy<T> {

    /**
     * 创建代理
     * @param interfaceClass 接口class
     * @param prop 属性
     * @param lazyLoadRegistry 懒加载服务注册
     * @param lazyStartRpcClient 懒加载rpc客户端
     * @return
     */
    T createProxy(Class<T> interfaceClass, Properties prop, boolean lazyLoadRegistry, boolean lazyStartRpcClient);
}
