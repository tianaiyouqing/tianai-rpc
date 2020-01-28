package cloud.tianai.rpc.core.client.proxy;

import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.client.RpcClient;
import cloud.tianai.rpc.core.client.impl.RpcClientImpl;
import cloud.tianai.rpc.core.constant.RpcClientConfigConstant;
import cloud.tianai.rpc.core.constant.RpcServerConfigConstant;
import cloud.tianai.rpc.core.factory.LoadBalanceFactory;
import cloud.tianai.rpc.core.factory.RegistryFactory;
import cloud.tianai.rpc.core.holder.RpcClientHolder;
import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RoundRobinLoadBalance;
import cloud.tianai.rpc.registory.api.NotifyListener;
import cloud.tianai.rpc.registory.api.Registry;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 11:44
 * @Description: RPC代理对象
 */
public abstract class AbstractRpcProxy<T> implements RpcProxy<T>, NotifyListener {
    /**
     * 用户得以参数.
     */
    protected Properties prop;
    /**
     * 接口的class类型.
     */
    protected Class<T> interfaceClass;
    /**
     * 服务注册.
     */
    protected Registry registry;
    /**
     * 当前URL.
     */
    protected URL url;
    /**
     * 订阅的url.
     */
    protected List<URL> subscribeUrls = Collections.emptyList();

    protected Object lock = new Object();
    /**
     * 请求超时
     */
    protected Integer requestTimeout;
    /**
     * 负载均衡器.
     */
    protected LoadBalance loadBalance;

    /***
     * 查看urls 并且订阅
     * @param url
     */
    protected void lookAndSubscribeUrl(URL url) {
        Result<List<URL>> lookup = registry.lookup(url);
        if (!lookup.isSuccess()) {
            throw new RpcException("无法从registry中拉取配置消息, e=" + lookup.getMsg() + ", code=" + lookup.getCode());
        }
        List<URL> urls = lookup.getData();
        if (CollectionUtils.isEmpty(urls)) {
            // 没有读取到urls
            throw new RpcException("无法读取到对应接口的注册地址, " + url);
        }
        this.subscribeUrls = urls;
        this.registry.subscribe(url, this);
    }

    /**
     * 启动RPC客户端
     *
     * @param url
     * @return
     */
    public RpcClient startRpcClient(URL url) {
        RpcClient rpcClient = new RpcClientImpl();
        rpcClient.start(prop, url);
        return rpcClient;
    }

    /**
     * 创建代理
     *
     * @return
     */
    @Override
    public T createProxy(Class<T> interfaceClass, Properties prop, boolean lazyLoadRegistry, boolean lazyStartRpcClient) {
        if (!interfaceClass.isInterface()) {
            // 如果不是接口，直接抛异常
            throw new IllegalArgumentException("创建rpc代理错误，class必须是接口");
        }
        this.interfaceClass = interfaceClass;
        this.prop = prop;
        this.url = new URL("tianai-rpc", "127.0.0.1", 0, interfaceClass.getName());
        if (!lazyLoadRegistry) {
            this.registry = startRegistry(prop);
            if (!lazyStartRpcClient) {
                // 读取到注册到注册器中的url
                List<URL> urls = lookUpOfThrow();
                for (URL url : urls) {
                    // 早期加载链接
                    getRpcClient(url);
                }
            }
        }
        // 添加轮询策略
        String loadBalanceName = prop.getProperty(RpcClientConfigConstant.LOAD_BALANCE, RoundRobinLoadBalance.NAME);
        loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceName);
        if (loadBalance == null) {
            throw new RpcException("未找到对应的轮询策略, loadBalanceName=" + loadBalanceName);
        }
        if (this.url != null && this.registry != null) {
            lookAndSubscribeUrl(this.url);
        }
        // 读取请求超时时间
        this.requestTimeout = Integer.valueOf(prop.getProperty(RpcClientConfigConstant.REQUEST_TIMEOUT,
                String.valueOf(RpcClientConfigConstant.DEFAULT_REQUEST_TIMEOUT)));

        return doCreateProxy();

    }

    /**
     * 创建代理对象
     *
     * @return
     */
    protected abstract T doCreateProxy();

    /**
     * 启动服务注册
     *
     * @param prop
     * @return
     */
    public static Registry startRegistry(Properties prop) {
        String registryProto = prop.getProperty(RpcClientConfigConstant.REGISTER);
        if (StringUtils.isBlank(registryProto)) {
            throw new IllegalArgumentException("无法读取到registry， 必须指定registry的protocol");
        }
        URL registryUrl = readRegistryConfiguration(prop);
        Registry registry = RegistryFactory.getRegistry(registryUrl);
        return registry;
    }


    public void startRegistryIfNecessary(Properties prop) {
        if (registry == null) {
            synchronized (lock) {
                if (registry == null) {
                    registry = startRegistry(prop);
                }
            }
        }
    }

    /**
     * 读取服务注册配置
     *
     * @param prop
     * @return
     */
    private static URL readRegistryConfiguration(Properties prop) {
        String registerHost = prop.getProperty(RpcServerConfigConstant.REGISTRY_HOST);
        if (StringUtils.isBlank(registerHost)) {
            throw new IllegalArgumentException("获取注册器失败，必须指定注册器地址");
        }
        String portStr = prop.getProperty(RpcServerConfigConstant.REGISTRY_PORT);
        String registryProtocol = prop.getProperty(RpcServerConfigConstant.REGISTER);
        if (StringUtils.isBlank(registryProtocol)) {
            throw new IllegalArgumentException("获取注册器失败，必须指定 [registryProtocol]");
        }
        Integer registerPort = null;
        if (StringUtils.isNoneBlank(portStr)) {
            registerPort = Integer.valueOf(portStr);
        }
        URL url = new URL(registryProtocol, registerHost, registerPort);
        return url;
    }

    @Override
    public void notify(List<URL> urls) {
        this.subscribeUrls = urls;
    }

    /**
     * 执行负载均衡
     *
     * @param request
     * @return
     */
    protected RpcClient loadBalance(Request request) {
        // 读取到注册到注册器中的url
        List<URL> urls = lookUpOfThrow();
        // 通过URL读取到对应的RpcClient
        List<RpcClient> rpcClients = getRpcClients(urls);
        // 通过负载均衡器拉取RpcClient
        RpcClient rpcClient = loadBalance.select(rpcClients, url, request);
        return rpcClient;
    }

    /**
     * 通过URLs 读取到对应的 RpcClients
     *
     * @param urls
     * @return
     */
    private List<RpcClient> getRpcClients(List<URL> urls) {
        List<RpcClient> rpcClients = new ArrayList<>(urls.size());
        for (URL u : urls) {
            try {
                rpcClients.add(getRpcClient(u));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rpcClients;
    }

    /**
     * 读取RpcClient
     *
     * @param url
     * @return
     */
    private RpcClient getRpcClient(URL url) {
        String protocol = url.getProtocol();
        String address = url.getAddress();
        RpcClient rpcClient = RpcClientHolder.getRpcClient(protocol, address);
        if (rpcClient != null) {
            return rpcClient;
        }
        synchronized (RpcClientHolder.getLock(url.getProtocol(), url.getAddress())) {
            rpcClient = RpcClientHolder.getRpcClient(protocol, address);
            if (rpcClient == null) {
                rpcClient = startRpcClient(url);
                RpcClientHolder.putRpcClient(url.getProtocol(), url.getAddress(), rpcClient);
            }
            return rpcClient;
        }
    }

    protected Request warpRequest(Object proxy, Method method, Object[] args) {
        Request request = new Request();
        request.setVersion("v1");
        request.setRequestParam(args);
        request.setMethodName(method.getName());
        request.setInterfaceType(interfaceClass);
        request.setReturnType(method.getReturnType());
        request.setHeartbeat(false);
        return request;
    }

    private List<URL> lookUpOfThrow() {
        if (CollectionUtils.isEmpty(subscribeUrls)) {
            lookAndSubscribeUrl(this.url);
        }
        if (CollectionUtils.isEmpty(subscribeUrls)) {
            throw new RpcException("注册器中无法读取到该URL [" + this.url + "] 对应的注册地址");
        }
        return subscribeUrls;
    }
}
