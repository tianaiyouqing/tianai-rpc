package cloud.tianai.rpc.core.bootstrap;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.configuration.RpcServerConfiguration;
import cloud.tianai.rpc.core.holder.RegistryHolder;
import cloud.tianai.rpc.core.holder.RpcServerHolder;
import cloud.tianai.rpc.core.util.RegistryUtils;
import cloud.tianai.rpc.registory.api.Registry;
import cloud.tianai.rpc.registory.api.exception.RpcRegistryException;
import cloud.tianai.rpc.remoting.api.*;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static cloud.tianai.rpc.common.constant.CommonConstant.*;

@Slf4j
public class ServerBootstrap {


    @Getter
    @Setter
    private URL serverURL = new URL(RPC_PROXY_PROTOCOL, IPUtils.getHostIp(), DEFAULT_SERVER_PORT);

    @Getter
    @Setter
    private Map<String, Object> parameters = new HashMap<>(16);

    @Getter
    @Setter
    private URL registryURL;

    @Getter
    @Setter
    private List<RpcInvocationPostProcessor> postProcessors = new LinkedList<>();

    /**
     * 远程Server.
     */
    @Getter
    private RemotingServer remotingServer;

    /** 服务注册. */
    @Getter
    private Registry registry;
    /**
     * 是否启动.
     */
    private AtomicBoolean start = new AtomicBoolean(false);

    private DefaultRpcInvocation rpcInvocation = new DefaultRpcInvocation();

    Map<URL, Object> temporaryObjectMap = new ConcurrentHashMap<>(256);

    public ServerBootstrap protocol(String protocol) {
        serverURL.setProtocol(protocol);
        return this;
    }

    public ServerBootstrap host(String host) {
        serverURL.setHost(host);
        return this;
    }

    public ServerBootstrap port(Integer port) {
        serverURL.setPort(port);
        return this;
    }

    public ServerBootstrap address(InetSocketAddress address) {
        String host = address.getHostString();
        int port = address.getPort();
        return host(host).port(port);
    }

    public ServerBootstrap codec(String codec) {
        parameters.put(CODEC_KEY, codec);
        return this;
    }

    public ServerBootstrap registry(URL registryConfig) {
        this.registryURL = registryConfig;
        return this;
    }

    public ServerBootstrap workThreads(Integer threads) {
        parameters.put(RPC_WORKER_THREADS_KEY, threads);
        return this;
    }

    public ServerBootstrap bossThreads(Integer threads) {
        parameters.put(RPC_BOSS_THREAD_KEY, threads);
        return this;
    }

    public ServerBootstrap timeout(Integer timeout) {
        parameters.put(TIMEOUT_KEY, timeout);
        return this;
    }


    public void start() {
        if (!start.compareAndSet(false, true)) {
            throw new RpcException("该服务已经启动，请勿重复启动[host=" + getServerURL().getHost() + ", port=" + getServerURL().getPort() + "]");
        }
        setDefaultParamsIfAbsent(parameters);
        // 设置参数
        serverURL.setParameters(CollectionUtils.toStringValueMap(parameters));
        // 启动远程server
        startRemotingServer();
        // 启动远程注册器
        startRegistry();
        // 设置为不可变的map
        temporaryObjectMap = Collections.unmodifiableMap(temporaryObjectMap);
        // 把临时ObjectMap放入registry中
        if (temporaryObjectMap.size() > 0) {
            temporaryObjectMap.forEach(this::register);
        }
    }

    private void setDefaultParamsIfAbsent(Map<String, Object> param) {
        param.putIfAbsent(TIMEOUT_KEY, DEFAULT_TIMEOUT);
        param.putIfAbsent(WEIGHT_KEY, DEFAULT_WEIGHT);
        param.putIfAbsent(CODEC_KEY, DEFAULT_CODEC);
        param.putIfAbsent(RPC_WORKER_THREADS_KEY, DEFAULT_IO_THREADS);
        param.putIfAbsent(RPC_BOSS_THREAD_KEY, DEFAULT_RPC_BOSS_THREAD);
        param.putIfAbsent(RPC_IDLE_TIMEOUT_KEY, DEFAULT_RPC_IDLE_TIMEOUT);
    }

    public RemotingChannelHolder getChannel() {
        if (remotingServer == null) {
            return null;
        }
        return remotingServer.getChannel();
    }


    public ServerBootstrap register(Class<?> interfaceClazz, Object ref) {
        register(interfaceClazz, ref, null);
        return this;
    }

    public ServerBootstrap register(Class<?> interfaceClazz, Object ref, Map<String, Object> parameters) {
        if(parameters != null && parameters.size() > 0) {
            // 转换成hashMap
            parameters = new HashMap<>(parameters);
        }else {
            parameters = new HashMap<>(8);
        }
        // 设置默认参数
        setDefaultParamsIfAbsent(parameters);
        // 设置连接超时
        parameters.putIfAbsent(RPC_CONNECT_TIMEOUT_KEY, DEFAULT_RPC_IDLE_TIMEOUT);

        URL url = new URL(RPC_PROXY_PROTOCOL,
                getServerURL().getHost(),
                getServerURL().getPort(),
                interfaceClazz.getName(),
                CollectionUtils.toStringValueMap(parameters));

        if (!isStart()) {
            // 没有启动的话，先存放到临时文件中
            temporaryObjectMap.put(url, ref);
            return this;
        }

        registry.register(url);
        rpcInvocation.putInvokeObj(interfaceClazz, ref);
        return this;
    }

    public ServerBootstrap register(URL url, Object ref) {
        if (!isStart()) {
            // 没有启动的话，先存放到临时文件中
            temporaryObjectMap.put(url, ref);
            return this;
        }
        Class<?> interfaceClazz;
        try {
            interfaceClazz = ClassUtils.forName(url.getPath());
        } catch (ClassNotFoundException e) {
            throw new RpcRegistryException("注册地址失败， path指定的值必须是class类型，且可被找到, path=" + url.getPath(), e);
        }
        registry.register(url);
        rpcInvocation.putInvokeObj(interfaceClazz, ref);
        return this;
    }

    public boolean isStart() {
        return start.get();
    }

    public String getServerAddress() {
        String host = getServerURL().getHost();
        Integer port = getServerURL().getPort();
        if (host == null) {
            host = IPUtils.getHostIp();
        }
        if (port < 0) {
            return host;
        }
        assert host != null;
        return host.concat(":").concat(String.valueOf(port));
    }

    private void startRemotingServer() {
        remotingServer = RpcServerHolder.computeIfAbsent(getServerURL().getProtocol(), getServerAddress(), (s, a) -> {
            // 通过加载器创建一个新的 远程Server， 不进行缓存
            RemotingServer r = ExtensionLoader.getExtensionLoader(RemotingServer.class).createExtension(s, false);
            if (Objects.isNull(r)) {
                throw new RpcException("未找到对应的远程server, server=" + s);
            }
            // 启动远程server
            // 添加RpcInvocation解析器
            if(CollectionUtils.isNotEmpty(postProcessors)) {
                postProcessors.forEach(rpcInvocation :: addPostProcessor);
            }

            // 配置解析器
            RemotingDataProcessor remotingDataProcessor =new RequestResponseRemotingDataProcessor(rpcInvocation);
            r.start(getServerURL(), remotingDataProcessor);
            return r;
        });
    }

//    private RemotingServerConfiguration getRemotingServerConfiguration() {
//        RemotingServerConfiguration remotingServerConfiguration = new RemotingServerConfiguration();
//        remotingServerConfiguration.setHost(prop.getHost());
//        remotingServerConfiguration.setPort(prop.getPort());
//        remotingServerConfiguration.setWorkerThreads(prop.getWorkerThread());
//        ExtensionLoader<RemotingDataCodec> extensionLoader = ExtensionLoader.getExtensionLoader(RemotingDataCodec.class);
//        // 通过扩展器加载 codec， 如果读不到直接抛出异常
//        RemotingDataCodec codec = extensionLoader.getExtension(prop.getCodec());
//
//        // 编码解码器
//        remotingServerConfiguration.setCodec(codec);
//        // 使用工厂创建解析器
//
//        remotingServerConfiguration.setRemotingDataProcessor(remotingDataProcessor);
//        remotingServerConfiguration.setConnectTimeout(prop.getTimeout());
//        remotingServerConfiguration.setServerIdleTimeout(prop.getIdleTimeout());
//        remotingServerConfiguration.setBossThreads(prop.getBossThreads());
//        return remotingServerConfiguration;
//    }

    private void startRegistry() {
        registry = RegistryHolder.computeIfAbsent(getRegistryURL(), RegistryUtils::createAndStart);
    }

    public void shutdown() {
        if (start.compareAndSet(true, false)) {
            if (remotingServer != null) {
                remotingServer.destroy();
            }
            if (registry != null) {
                registry.shutdown();
            }
        }
    }

    public void addRpcInvocationPostProcessor(RpcInvocationPostProcessor rpcInvocationPostProcessor) {
        postProcessors.remove(rpcInvocationPostProcessor);
        postProcessors.add(rpcInvocationPostProcessor);
    }
}
