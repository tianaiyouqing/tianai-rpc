package cloud.tianai.rpc.core.server.impl;

import cloud.tianai.remoting.api.*;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.core.server.RpcServer;
import cloud.tianai.rpc.core.server.constant.RpcServerConfigConstant;
import cloud.tianai.rpc.core.server.remoting.CodecFactory;
import cloud.tianai.rpc.core.server.remoting.RegistryFactory;
import cloud.tianai.rpc.core.server.remoting.RemotingServerFactory;
import cloud.tianai.rpc.registory.api.Registry;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;


public class RpcServerImpl implements RpcServer, RpcInvocation {

    /** 是否启动. */
    private AtomicBoolean start = new AtomicBoolean(false);

    private String host;
    private Integer port;

    private RemotingServer remotingServer;
    private RemotingChannelHolder remotingChannelHolder;
    private RemotingServerConfiguration remotingServerConfiguration;

    private Registry registry;

    private Map<Class<?>, Object> objectMap = new HashMap<>();

    @Override
    public void start(Properties prop) throws RpcException {
        if(!start.compareAndSet(false, true)) {
            throw new RpcException("该服务已经启动，请勿重复启动[host="+host+", port="+port+"]");
        }
        // 启动远程server
        startRemotingServer(prop);
        // 启动远程注册器
        startRegistry(prop);
    }

    private void startRegistry(Properties prop) {
        String registryProtocol = prop.getProperty(RpcServerConfigConstant.REGISTER, RpcServerConfigConstant.DEFAULT_REGISTER);
        Registry registry = RegistryFactory.getRegistry(registryProtocol);
        if(registry == null) {
            throw new RpcException("未找到对应的注册工厂, protocol=" + registryProtocol);
        }

        URL registryUrl = readRegistryConfiguration(registryProtocol, prop);
        this.registry = registry.start(registryUrl);
    }

    private URL readRegistryConfiguration(String registryProtocol , Properties prop) {
        String registerHost = prop.getProperty(RpcServerConfigConstant.REGISTRY_HOST, "127.0.0.1");
        String portStr = prop.getProperty(RpcServerConfigConstant.REGISTRY_PORT);
        Integer registerPort = null;
        if(StringUtils.isNoneBlank(portStr)) {
            registerPort = Integer.valueOf(portStr);
        }

        URL url = new URL(registryProtocol, registerHost, registerPort);
        return url;
    }

    private void startRemotingServer(Properties prop) {
        // 创建远程server
        String protocol = prop.getProperty(RpcServerConfigConstant.PROTOCOL, RpcServerConfigConstant.DEFAULT_PROTOCOL);
        remotingServer = RemotingServerFactory.create(protocol);
        if(Objects.isNull(remotingServer)) {
            throw new RpcException("未找到对应的远程server, protocol=" + protocol);
        }
        RemotingServerConfiguration conf = readRemotingServerConfiguration(prop);
        this.remotingServerConfiguration = conf;
        this.host = conf.getHost();
        this.port = conf.getPort();
        remotingChannelHolder = remotingServer.start(conf);
    }

    private RemotingServerConfiguration readRemotingServerConfiguration(Properties prop) {
        RemotingServerConfiguration configuration = new RemotingServerConfiguration();

        String host = prop.getProperty(RpcServerConfigConstant.HOST, "127.0.0.1");
        Integer port = Integer.valueOf(prop.getProperty(RpcServerConfigConstant.PORT, String.valueOf(20880)));
        Integer workerThreads = Integer.valueOf(prop.getProperty(RpcServerConfigConstant.WORKER_THREADS, String.valueOf(12)));
        Integer bossThreads = Integer.valueOf(prop.getProperty(RpcServerConfigConstant.BOSS_THREADS, String.valueOf(1)));
        Integer timeout = Integer.valueOf(prop.getProperty(RpcServerConfigConstant.TIMEOUT, String.valueOf(5000)));
        String codecProtocol = prop.getProperty(RpcServerConfigConstant.CODEC, RpcServerConfigConstant.DEFAULT_CODEC);
        // 读取编码解码器
        KeyValue<RemotingDataEncoder, RemotingDataDecoder> codec = CodecFactory.getCodec(codecProtocol);
        if(Objects.isNull(codec) || !codec.isNotEmpty()) {
            // 未找到编码解码器
            throw new RpcException("未找到编码解码器， protocol=" + codecProtocol);
        }

        configuration.setHost(host);
        configuration.setPort(port);
        configuration.setWorkerThreads(workerThreads);
        configuration.setBossThreads(bossThreads);
        configuration.setEncoder(codec.getKey());
        configuration.setDecoder(codec.getValue());
        configuration.setIdleTimeout(timeout);
        configuration.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(this));
        return configuration;
    }

    @Override
    public <T> void register(Class<T> interfaceClazz, T ref) {
        URL url = new URL(remotingServer.getRemotingType(),
                remotingServerConfiguration.getHost(),
                remotingServerConfiguration.getPort(),
                interfaceClazz.getName());

        registry.register(url);
        objectMap.remove(interfaceClazz);
        objectMap.put(interfaceClazz, ref);
    }


    @Override
    public Object invoke(Request request) {
        Class<?> interfaceType = request.getInterfaceType();
        Object invokeObj = objectMap.get(interfaceType);
        String methodName = request.getMethodName();
        if(Objects.isNull(invokeObj)) {
            throw new RpcException("rpc调用异常, 未找到对应的实例[" + interfaceType +"]");
        }
        Object[] requestParam = request.getRequestParam();
        Class<?>[] requestParamType = ClassUtils.getType(requestParam);
        try {
            Method method = invokeObj.getClass().getMethod(methodName, requestParamType);
            Object res = method.invoke(invokeObj, requestParam);
            return res;
        } catch (Exception e) {
            // 直接抛出去
            throw new RpcException(e);
        }
    }
}
