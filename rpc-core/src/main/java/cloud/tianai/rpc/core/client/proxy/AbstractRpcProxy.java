package cloud.tianai.rpc.core.client.proxy;

import cloud.tianai.remoting.api.*;
import cloud.tianai.remoting.api.exception.RpcChannelClosedException;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.constant.CommonConstant;
import cloud.tianai.rpc.core.constant.RpcClientConfigConstant;
import cloud.tianai.rpc.core.constant.RpcConfigConstant;
import cloud.tianai.rpc.core.constant.RpcServerConfigConstant;
import cloud.tianai.rpc.core.factory.LoadBalanceFactory;
import cloud.tianai.rpc.core.factory.RemotingClientFactory;
import cloud.tianai.rpc.core.holder.RegistryHolder;
import cloud.tianai.rpc.core.holder.RpcClientHolder;
import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RoundRobinLoadBalance;
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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

    protected Object lock = new Object();
    /**
     * 请求超时
     */
    protected Integer requestTimeout;
    /**
     * 负载均衡器.
     */
    protected LoadBalance loadBalance;

    protected Properties prop;

    protected RemotingClient remotingClient;

    /**
     * 默认重试次数3次.
     */
    public static final int DEFAULT_REQUEST_RETRY = 3;

    /**
     * 服务注册连接时默认重试次数.
     */
    public static final int DEFAULT_REGISTRY_RETRY = 3;
    /**
     * 请求重试次数.
     */
    protected int retry = DEFAULT_REQUEST_RETRY;

    protected Integer registryRetry = DEFAULT_REGISTRY_RETRY;


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
        this.prop = prop;
        this.interfaceClass = interfaceClass;
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
    protected Registry startRegistry(Properties prop) {
        String registryProto = prop.getProperty(RpcClientConfigConstant.REGISTER);
        if (StringUtils.isBlank(registryProto)) {
            throw new IllegalArgumentException("无法读取到registry， 必须指定registry的protocol");
        }
        URL registryUrl = readRegistryConfiguration(prop);

        Registry registry = RegistryHolder.computeIfAbsent(registryUrl, RegistryUtils::createAndStart);
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
        // 重试次数
        String retryStr = prop.getProperty(CommonConstant.RETRY);

        if (StringUtils.isBlank(registryProtocol)) {
            throw new IllegalArgumentException("获取注册器失败，必须指定 [registryProtocol]");
        }
        Integer registerPort = null;
        if (StringUtils.isNoneBlank(portStr)) {
            registerPort = Integer.valueOf(portStr);
        }
        URL url = new URL(registryProtocol, registerHost, registerPort);
        if (StringUtils.isNotBlank(retryStr)) {
            // 添加重试次数
            url.addParameter(CommonConstant.RETRY, retryStr);
        }
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
    protected RemotingClient loadBalance(Request request) {
        // 读取到注册到注册器中的url
        List<URL> urls = lookUpOfThrow();
        // 通过URL读取到对应的RpcClient
        List<RemotingClient> rpcClients = getRpcClients(urls);
        // 通过负载均衡器拉取RpcClient
        RemotingClient rpcClient = loadBalance.select(rpcClients, url, request);
        if (!rpcClient.isOpen()) {
            // 如果通道已经关闭，进行重连试试
            // todo 最好做成异步重连
            rpcClient.doConnect();
        }
        return rpcClient;
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
            int workThreads = Integer.parseInt(url.getParameter(RpcClientConfigConstant.WORKER_THREADS, String.valueOf(RpcConfigConstant.DEFAULT_IO_THREADS)));

            String codecProtocol = prop.getProperty(RpcClientConfigConstant.CODEC, RpcClientConfigConstant.DEFAULT_CODEC);
            KeyValue<RemotingDataEncoder, RemotingDataDecoder> codec = getCodec(codecProtocol);
            if (codec == null || !codec.isNotEmpty()) {
                throw new RpcException("未找到对应的codec， protocol=" + codecProtocol);
            }
            Integer timeout = Integer.valueOf(prop.getProperty(RpcClientConfigConstant.TIMEOUT, String.valueOf(5000)));
            RemotingConfiguration conf = new RemotingConfiguration();
            conf.setHost(host);
            conf.setPort(port);
            conf.setWorkerThreads(workThreads);
            conf.setEncoder(codec.getKey());
            conf.setDecoder(codec.getValue());
            conf.setConnectTimeout(timeout);
            conf.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(new HeartbeatRpcInvocation()));
            String client = prop.getProperty(RpcClientConfigConstant.PROTOCOL, RpcClientConfigConstant.DEFAULT_PROTOCOL);
            RemotingClient c = RemotingClientFactory.create(client);
            // 启动客户端
            c.start(conf);
            return c;
        });
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

    /**
     * 执行请求
     *
     * @param request 请求体
     * @return Object
     * @throws TimeoutException 重试次数达到后如果还请求不到，理应直接抛出异常
     */
    protected Object request(RemotingClient rpcClient, Request request) throws TimeoutException {
        // 通过负载均衡读取到对应的rpcClient
        // 如果请求超时，理应再从负载均衡器里拿一个连接执行重试
        CompletableFuture<Object> future = rpcClient.getchannel().request(request, requestTimeout);
        Object resObj = null;
        try {
            resObj = future.get(requestTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RpcChannelClosedException) {
                if (!rpcClient.isActive()) {
                    synchronized (lock) {
                        // 休眠100毫秒重试一下
                        if (!rpcClient.isActive()) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(100);
                            } catch (InterruptedException ex) {
                                // 不做处理
                            }
                            // 这里应该同步处理
                            try {
                                rpcClient.doConnect();
                            } catch (RpcRemotingException ex) {
                                throw new TimeoutException(ex.getMessage());
                            }
                        }
                    }
                }
                // 如果连接成功,重新请求
                if (rpcClient.isActive()) {
                    resObj = request(rpcClient, request);
                } else {
                    throw new TimeoutException(e.getCause().getMessage());
                }
            } else {
                throw new TimeoutException(e.getCause().getMessage());
            }
        }
        return resObj;
    }

    protected Object retryRequest(RemotingClient rpcClient, Request request, int currRetry) throws TimeoutException {
        // 负责继续请求重试
        try {
            Object res = request(loadBalance(request), request);
            return res;
        } catch (TimeoutException e) {
            currRetry++;
            // 如果超过重试次数， 直接抛异常
            log.info("请求重试, 请求体 [{}], 当前已重试次数{}", request, currRetry);
            if (currRetry > retry) {
                throw new TimeoutException("请求失败， 超过最大重试次数");
            }
            // 休眠100毫秒重试一下
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {
                // 不做处理
            }
            log.info("请求重试, 请求体 [{}], 当前已重试次数{}", request, currRetry);
            return retryRequest(rpcClient, request, currRetry);
        }
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
}
