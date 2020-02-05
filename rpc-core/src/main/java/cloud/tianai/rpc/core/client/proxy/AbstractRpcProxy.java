package cloud.tianai.rpc.core.client.proxy;

import cloud.tianai.remoting.api.*;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.*;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.factory.LoadBalanceFactory;
import cloud.tianai.rpc.core.factory.RemotingClientFactory;
import cloud.tianai.rpc.core.holder.RegistryHolder;
import cloud.tianai.rpc.core.holder.RpcClientHolder;
import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RoundRobinLoadBalance;
import cloud.tianai.rpc.core.template.RpcClientTemplate;
import cloud.tianai.rpc.core.util.RegistryUtils;
import cloud.tianai.rpc.registory.api.NotifyListener;
import cloud.tianai.rpc.registory.api.Registry;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static cloud.tianai.rpc.core.factory.CodecFactory.getCodec;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 11:44
 * @Description: RPC代理对象
 */
@Slf4j
public abstract class AbstractRpcProxy<T> implements RpcProxy<T>, NotifyListener {
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

    protected final Object lock = new Object();
    /**
     * 请求超时
     */
    protected Integer requestTimeout;
    /**
     * 负载均衡器.
     */
    protected LoadBalance loadBalance;

    protected RpcClientConfiguration rpcConfiguration;

    /**
     * 默认重试次数3次.
     */
    public static final int DEFAULT_REQUEST_RETRY = 3;

    /**
     * 请求重试次数.
     */
    protected int retry = DEFAULT_REQUEST_RETRY;

    protected RpcClientTemplate rpcClientTemplate;

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
        log.info("TIAN-RPC REGISTRY {}拉取到消息:{}", this.registry.getProtocol(), urls);
        this.registry.subscribe(url, this);
    }


    /**
     * 创建代理
     *
     * @return
     */
    @Override
    public T createProxy(Class<T> interfaceClass, RpcClientConfiguration conf, boolean lazyLoadRegistry, boolean lazyStartRpcClient) {
        if (!interfaceClass.isInterface()) {
            // 如果不是接口，直接抛异常
            throw new IllegalArgumentException("创建rpc代理错误，class必须是接口");
        }
        this.rpcConfiguration = conf;
        this.interfaceClass = interfaceClass;
        this.url = new URL("tianai-rpc", "127.0.0.1", 0, interfaceClass.getName());
        if (!lazyLoadRegistry) {
            this.registry = startRegistry(conf.getRegistryUrl());
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
        String loadBalanceName = conf.getOrDefault(conf.getLoadBalance(), RoundRobinLoadBalance.NAME);
        loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceName);
        if (loadBalance == null) {
            throw new RpcException("未找到对应的轮询策略, loadBalanceName=" + loadBalanceName);
        }
        if (this.url != null && this.registry != null) {
            lookAndSubscribeUrl(this.url);
        }
        // 读取请求超时时间
        this.requestTimeout = conf.getRequestTimeout();
        // 构建rpcClientTemplate
        rpcClientTemplate = new RpcClientTemplate(requestTimeout, retry, lock);
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
     * @param registryUrl
     * @return
     */
    protected Registry startRegistry(URL registryUrl) {
        Registry registry = RegistryHolder.computeIfAbsent(registryUrl, RegistryUtils::createAndStart);
        registry.subscribe(() -> {
            // 重新拉取
            lookAndSubscribeUrl(url);
        });
        return registry;
    }


    public void startRegistryIfNecessary(URL registryUrl) {
        if (registry == null) {
            synchronized (lock) {
                if (registry == null) {
                    registry = startRegistry(registryUrl);
                }
            }
        }
    }

    @Override
    public void notify(List<URL> urls) {
        log.debug("[registry] 订阅到消息: {}", urls);
        this.subscribeUrls = urls;
    }

    /**
     * 执行负载均衡
     *
     * @param request
     * @return
     */
    protected RemotingClient loadBalance(Request request) {
        synchronized (lock) {
            // 读取到注册到注册器中的url
            List<URL> urls = lookUpOfThrow();
            // 通过URL读取到对应的RpcClient
            List<RemotingClient> rpcClients = getRpcClients(urls);
            // 通过负载均衡器拉取RpcClient
            RemotingClient rpcClient = loadBalance.select(rpcClients, url, request);
            return rpcClient;
        }
    }

    private List<RemotingClient> getRpcClients(List<URL> urls) {
        List<RemotingClient> result = new ArrayList<>(urls.size());
        for (URL u : urls) {
            result.add(getRpcClient(u));
        }
        return result;
    }


    private RemotingClient getRpcClient(URL url) {
        return RpcClientHolder.computeIfAbsent(url.getProtocol(), url.getAddress(), (p, a) -> {
            String host = url.getHost();
            if (StringUtils.isBlank(host)) {
                throw new RpcException("客户端启动失败，必须指定host");
            }
            Integer port = url.getPort();
            int workThreads = rpcConfiguration.getWorkerThread();
            String codecProtocol = rpcConfiguration.getOrDefault(rpcConfiguration.getCodec(), CommonConstant.DEFAULT_CODEC);
            KeyValue<RemotingDataEncoder, RemotingDataDecoder> codec = getCodec(codecProtocol);
            if (codec == null || !codec.isNotEmpty()) {
                throw new RpcException("未找到对应的codec， protocol=" + codecProtocol);
            }
            Integer timeout = rpcConfiguration.getOrDefault(rpcConfiguration.getTimeout(), CommonConstant.DEFAULT_TIMEOUT);
            RemotingConfiguration conf = new RemotingConfiguration();
            conf.setHost(host);
            conf.setPort(port);
            conf.setWorkerThreads(workThreads);
            conf.setEncoder(codec.getKey());
            conf.setDecoder(codec.getValue());
            conf.setConnectTimeout(timeout);
            conf.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(new HeartbeatRpcInvocation()));
            String client = rpcConfiguration.getProtocol();
            RemotingClient c = RemotingClientFactory.create(client);
            // 启动客户端
            if (c != null) {
                c.start(conf);
            } else {
                throw new RpcRemotingException("无法创建对应的 远程客户端 ， client=" + client);
            }
            return c;
        });
    }

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

    protected Object retryRequest(RemotingClient rpcClient, Request request) throws TimeoutException {
        return rpcClientTemplate.retryRequest(rpcClient, request, this::loadBalance);
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

    public static class HeartbeatRpcInvocation implements RpcInvocation {
        @Override
        public Object invoke(Request request) {
            if (request.isHeartbeat()) {
                return "heartbeat success";
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "tianai-rpc-proxy{" +
                "interfaceClass=" + interfaceClass +
                ", registry=" + registry +
                ", url=" + url +
                ", requestTimeout=" + requestTimeout +
                ", prop=" + rpcConfiguration +
                '}';
    }
}
