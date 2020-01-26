package cloud.tianai.rpc.core.server;

import cloud.tianai.rpc.common.exception.RpcException;

import java.util.Properties;

public interface RpcServer {

    /**`
     * 启动 rpcServer
     * @param  prop 启动所需参数
     * @throws RpcException
     */
    void start(Properties prop) throws RpcException;

    /**
     * 注册
     * @param interfaceClazz 注册的接口
     * @param ref 对应接口的实现
     * @param <T>
     */
    <T> void register(Class<T> interfaceClazz,  T ref);
}
