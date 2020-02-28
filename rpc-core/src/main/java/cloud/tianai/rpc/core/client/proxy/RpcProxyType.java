package cloud.tianai.rpc.core.client.proxy;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/28 23:30
 * @Description: RPC代理类型支持
 */
public enum  RpcProxyType {

    /** JDK代理. */
    JDK_PROXY,
    /** 字节码代理. */
    JAVASSIST_PROXY
}
