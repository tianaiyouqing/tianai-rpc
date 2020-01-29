package cloud.tianai.rpc.core.bootstrap;

import cloud.tianai.remoting.api.*;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.factory.CodecFactory;
import cloud.tianai.rpc.core.factory.RegistryFactory;
import cloud.tianai.rpc.core.factory.RemotingServerFactory;
import cloud.tianai.rpc.core.holder.RegistryHolder;
import cloud.tianai.rpc.core.holder.RpcServerHolder;
import cloud.tianai.rpc.core.util.RegistryUtils;
import cloud.tianai.rpc.registory.api.Registry;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ServerBootstrap {

    /**
     * 网络抖动造成的重试次数.
     */
    public static final int REGISTRY_RETRY = 5;

    private RemotingServerConfiguration prop = new RemotingServerConfiguration();
    private URL registryUrl = new URL();
    private String server = "netty";
    private String codec;


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

    public ServerBootstrap server(String server) {
        this.server = server;
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
        this.codec = codec;
        return this;
    }

    public ServerBootstrap registry(URL registryConfig) {
        this.registryUrl = registryConfig;
        return this;
    }

    public ServerBootstrap workThreads(Integer threads) {
        prop.setWorkerThreads(threads);
        return this;
    }

    public ServerBootstrap bossThreads(Integer threads) {
        prop.setBossThreads(threads);
        return this;
    }

    public ServerBootstrap timeout(Integer timeout) {
        prop.setConnectTimeout(timeout);
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
    }

    public RemotingChannelHolder getChannel() {
        if (remotingServer == null) {
            return null;
        }
        return remotingServer.getchannel();
    }

    public <T> void register(Class<T> interfaceClazz, T ref) {
        if (!isStart()) {
            throw new RpcException("必须先执行 start() 方法");
        }
        URL url = new URL(remotingServer.getRemotingType(),
                prop.getHost(),
                prop.getPort(),
                interfaceClazz.getName());

        registry.register(url);
        rpcInvocation.put(interfaceClazz, ref);
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
        remotingServer = RpcServerHolder.computeIfAbsent(server, getServerAddress(), (s, a) -> {
            RemotingServer r = RemotingServerFactory.create(s);
            if (Objects.isNull(r)) {
                throw new RpcException("未找到对应的远程server, server=" + s);
            }
            // 启动远程server
            // 配置解析器
            prop.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(rpcInvocation));
            KeyValue<RemotingDataEncoder, RemotingDataDecoder> encoderAndDecoder = CodecFactory.getCodec(this.codec);
            if (encoderAndDecoder == null || !encoderAndDecoder.isNotEmpty()) {
                throw new RpcException("未找到对应的codec， codec=".concat(this.codec));
            }
            // 编码解码器
            prop.setEncoder(encoderAndDecoder.getKey());
            prop.setDecoder(encoderAndDecoder.getValue());
            r.start(prop);
            return r;
        });
    }

    private void startRegistry() {
        registry = RegistryHolder.computeIfAbsent(registryUrl, RegistryUtils::createAndStart);
    }

    public void shutdown() {
        if (start.compareAndSet(true, false)) {
            if (remotingServer != null) {
                remotingServer.stop();
            }
        }
    }

    public RemotingServerConfiguration getServerProp() {
        return prop;
    }

    public URL getRegistryUrl() {
        return registryUrl;
    }

}
