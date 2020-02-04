package cloud.tianai.rpc.core.bootstrap;

import cloud.tianai.remoting.api.*;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.RpcServerConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.factory.CodecFactory;
import cloud.tianai.rpc.core.factory.RemotingServerFactory;
import cloud.tianai.rpc.core.holder.RegistryHolder;
import cloud.tianai.rpc.core.holder.RpcServerHolder;
import cloud.tianai.rpc.core.util.RegistryUtils;
import cloud.tianai.rpc.registory.api.Registry;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ServerBootstrap {


    @Getter
    @Setter
    private RpcServerConfiguration prop = new RpcServerConfiguration();

    /**
     * 远程Server.
     */
    private RemotingServer remotingServer;
    private Registry registry;
    private RemotingChannelHolder channelHolder;
    /**
     * 是否启动.
     */
    private AtomicBoolean start = new AtomicBoolean(false);

    private DefaultRpcInvocation rpcInvocation = new DefaultRpcInvocation();
    Map<Class<?>, Object> temporaryObjectMap = new ConcurrentHashMap<>(256);

    public ServerBootstrap server(String server) {
        prop.setProtocol(server);
        return this;
    }

    public ServerBootstrap host(String host) {
        prop.setHost(host);
        return this;
    }

    public ServerBootstrap port(Integer port) {
        prop.setPort(port);
        return this;
    }

    public ServerBootstrap address(InetSocketAddress address) {
        String host = address.getHostString();
        int port = address.getPort();
        return host(host).port(port);
    }

    public ServerBootstrap codec(String codec) {
        prop.setCodec(codec);
        return this;
    }

    public ServerBootstrap registry(URL registryConfig) {
        prop.setRegistryUrl(registryConfig);
        return this;
    }

    public ServerBootstrap workThreads(Integer threads) {
        prop.setWorkerThread(threads);
        return this;
    }

    public ServerBootstrap bossThreads(Integer threads) {
        prop.setBossThreads(threads);
        return this;
    }

    public ServerBootstrap timeout(Integer timeout) {
        prop.setTimeout(timeout);
        return this;
    }


    public void start() {
        if (!start.compareAndSet(false, true)) {
            throw new RpcException("该服务已经启动，请勿重复启动[host=" + prop.getHost() + ", port=" + prop.getPort() + "]");
        }
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

    public RemotingChannelHolder getChannel() {
        if (remotingServer == null) {
            return null;
        }
        return remotingServer.getchannel();
    }

    public ServerBootstrap register(Class<?> interfaceClazz, Object ref) {
        if (!isStart()) {
            // 没有启动的话，先存放到临时文件中
            temporaryObjectMap.put(interfaceClazz, ref);
            return this;
        }
        URL url = new URL(remotingServer.getRemotingType(),
                prop.getHost(),
                prop.getPort(),
                interfaceClazz.getName());

        registry.register(url);
        rpcInvocation.put(interfaceClazz, ref);
        return this;
    }

    public boolean isStart() {
        return start.get();
    }

    public String getServerAddress() {
        String host = prop.getHost();
        Integer port = prop.getPort();
        if (host == null) {
            host = IPUtils.getHostIp();
        }
        if (port == null) {
            return host;
        }
        return host.concat(":").concat(String.valueOf(port));
    }

    private void startRemotingServer() {
        remotingServer = RpcServerHolder.computeIfAbsent(prop.getProtocol(), getServerAddress(), (s, a) -> {
            RemotingServer r = RemotingServerFactory.create(s);
            if (Objects.isNull(r)) {
                throw new RpcException("未找到对应的远程server, server=" + s);
            }
            // 启动远程server
            // 配置解析器
            RemotingServerConfiguration conf = getRemotingServerConfiguration();
            r.start(conf);
            return r;
        });
    }

    private RemotingServerConfiguration getRemotingServerConfiguration() {
        RemotingServerConfiguration remotingServerConfiguration = new RemotingServerConfiguration();
        remotingServerConfiguration.setHost(prop.getHost());
        remotingServerConfiguration.setPort(prop.getPort());
        remotingServerConfiguration.setWorkerThreads(prop.getWorkerThread());
        KeyValue<RemotingDataEncoder, RemotingDataDecoder> encoderAndDecoder = CodecFactory.getCodec(prop.getCodec());
        if (encoderAndDecoder == null || !encoderAndDecoder.isNotEmpty()) {
            throw new RpcException("未找到对应的codec， codec=".concat(prop.getCodec()));
        }
        // 编码解码器
        remotingServerConfiguration.setEncoder(encoderAndDecoder.getKey());
        remotingServerConfiguration.setDecoder(encoderAndDecoder.getValue());
        remotingServerConfiguration.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(rpcInvocation));
        remotingServerConfiguration.setConnectTimeout(prop.getTimeout());
        remotingServerConfiguration.setIdleTimeout(prop.getTimeout());
        remotingServerConfiguration.setBossThreads(prop.getBossThreads());
        return remotingServerConfiguration;
    }

    private void startRegistry() {
        registry = RegistryHolder.computeIfAbsent(prop.getRegistryUrl(), RegistryUtils::createAndStart);
    }

    public void shutdown() {
        if (start.compareAndSet(true, false)) {
            if (remotingServer != null) {
                remotingServer.stop();
            }
        }
    }
}
