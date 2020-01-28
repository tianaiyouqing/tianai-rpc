package cloud.tianai.rpc.core.server.impl;

import cloud.tianai.remoting.api.*;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.core.constant.RpcConfigConstant;
import cloud.tianai.rpc.core.server.RpcServer;
import cloud.tianai.rpc.core.constant.RpcServerConfigConstant;
import cloud.tianai.rpc.core.factory.CodecFactory;
import cloud.tianai.rpc.core.factory.RegistryFactory;
import cloud.tianai.rpc.core.factory.RemotingServerFactory;
import cloud.tianai.rpc.registory.api.Registry;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 11:27
 * @Description: rpc server实现
 */
public class RpcServerImpl implements RpcServer, RpcInvocation {

    /** 是否启动. */
    private AtomicBoolean start = new AtomicBoolean(false);
    /** ip. */
    private String host;
    /** 端口. */
    private Integer port;

    /** 远程Server. */
    private RemotingServer remotingServer;
    /** 远程管道持有. */
    private RemotingChannelHolder remotingChannelHolder;
    /** 远程server配置. */
    private RemotingServerConfiguration remotingServerConfiguration;
    /** 服务注册. */
    private Registry registry;

    /** 注册rpc对象  接口Class -> 对应实现. */
    private Map<Class<?>, Object> objectMap = new ConcurrentHashMap<>(256);


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

    /**
     * 启动服务注册
     * @param prop 启动时所需参数
     */
    private void startRegistry(Properties prop) {
        URL registryUrl = readRegistryConfiguration(prop);
        registry = RegistryFactory.getRegistry(registryUrl);
        if(registry == null) {
            throw new RpcException("未找到对应的注册工厂, protocol=" + registryUrl.getProtocol());
        }
//        this.registry = registry.start(registryUrl);
    }

    /**
     * 通过 properties读取到 服务注册对应的参数
     * @param prop
     * @return
     */
    private URL readRegistryConfiguration(Properties prop) {
        String registerHost = prop.getProperty(RpcServerConfigConstant.REGISTRY_HOST, "127.0.0.1");
        String portStr = prop.getProperty(RpcServerConfigConstant.REGISTRY_PORT);
        String registryProtocol = prop.getProperty(RpcServerConfigConstant.REGISTER, RpcServerConfigConstant.DEFAULT_REGISTER);
        Integer registerPort = null;
        if(StringUtils.isNoneBlank(portStr)) {
            registerPort = Integer.valueOf(portStr);
        }
        URL url = new URL(registryProtocol, registerHost, registerPort);
        return url;
    }

    /**
     * 启动远程 server
     * @param prop
     */
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

    /**
     * 读取远程server配置信息
     * @param prop
     * @return
     */
    private RemotingServerConfiguration readRemotingServerConfiguration(Properties prop) {
        RemotingServerConfiguration configuration = new RemotingServerConfiguration();

        String host = prop.getProperty(RpcServerConfigConstant.HOST, "127.0.0.1");
        Integer port = Integer.valueOf(prop.getProperty(RpcServerConfigConstant.PORT, String.valueOf(20880)));
        Integer workerThreads = Integer.valueOf(prop.getProperty(RpcServerConfigConstant.WORKER_THREADS, String.valueOf(RpcConfigConstant.DEFAULT_IO_THREADS)));
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

    /**
     * 注册
     * @param interfaceClazz 注册的接口
     * @param ref 对应接口的实现
     * @param <T>
     */
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
