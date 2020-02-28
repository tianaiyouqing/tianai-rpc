package cloud.tianai.rpc.core.client.proxy;

import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.template.DefaultRpcClientTemplate;
import cloud.tianai.rpc.core.template.RpcClientTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static cloud.tianai.rpc.common.constant.CommonConstant.DEFAULT_REQUEST_RETRY;
import static cloud.tianai.rpc.common.constant.CommonConstant.DEFAULT_REQUEST_TIMEOUT;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 11:44
 * @Description: RPC代理对象
 */
@Slf4j
public abstract class AbstractRpcProxy<T> implements RpcProxy<T> {

    public static final String TO_STRING_FUN_NAME = "toString";
    public static final String HASH_CODE_FUN_NAME = "hashCode";
    public static final String EQUALS_FUN_NAME = "equals";

    /**
     * 接口的class类型.
     */
    protected Class<T> interfaceClass ;
    /**
     * 当前URL.
     */
    protected URL url;
    /**
     * 请求超时
     */
    protected Integer requestTimeout;

    protected RpcClientConfiguration rpcConfiguration;

    /**
     * 请求重试次数.
     */
    protected int retry;

    @Getter
    protected RpcClientTemplate rpcClientTemplate;

    /**
     * 创建代理
     *
     * @return
     */
    @Override
    public T createProxy(Class<T> interfaceClass, RpcClientConfiguration conf) {
        if (!interfaceClass.isInterface()) {
            // 如果不是接口，直接抛异常
            throw new IllegalArgumentException("创建rpc代理错误，class必须是接口");
        }
        this.rpcConfiguration = conf;
        this.interfaceClass = interfaceClass;
        this.requestTimeout = conf.getOrDefault(conf.getRequestTimeout(), DEFAULT_REQUEST_TIMEOUT);
        this.retry = conf.getOrDefault(conf.getRetry(), DEFAULT_REQUEST_RETRY);
        this.url = new URL("tianai-rpc", IPUtils.getHostIp(), 0, interfaceClass.getName());
        // 构建RpcClient模板
        rpcClientTemplate = createRpcClientTemplate(conf, conf.isLazyLoadRegistry(), conf.isLazyStartRpcClient());
        return doCreateProxy();

    }

    private RpcClientTemplate createRpcClientTemplate(RpcClientConfiguration conf, boolean lazyLoadRegistry, boolean lazyStartRpcClient) {
        return new DefaultRpcClientTemplate(rpcConfiguration, url, lazyLoadRegistry, lazyStartRpcClient, true);
    }

    /**
     * 创建代理对象
     *
     * @return
     */
    protected abstract T doCreateProxy();

    protected Request warpRequest(Object proxy, Method method, Object[] args) {
        Request request = new Request();
        request.setVersion("v1")
                .setRequestParam(args)
                .setMethodName(method.getName())
                .setInterfaceType(interfaceClass)
                .setReturnType(method.getReturnType())
                .setHeartbeat(false);
        return request;
    }


    @Override
    public String toString() {
        return "tianai-rpc-proxy{" +
                "interfaceClass=" + interfaceClass +
                ", url=" + url +
                ", requestTimeout=" + requestTimeout +
                ", rpcConfiguration=" + rpcConfiguration +
                ", retry=" + retry +
                ", rpcClientTemplate=" + rpcClientTemplate +
                '}';
    }
}
