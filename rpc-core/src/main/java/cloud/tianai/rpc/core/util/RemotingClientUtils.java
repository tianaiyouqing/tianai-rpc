package cloud.tianai.rpc.core.util;

import cloud.tianai.remoting.api.*;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.core.bootstrap.Bootstrap;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.factory.CodecFactory;
import cloud.tianai.rpc.core.factory.RemotingClientFactory;
import cloud.tianai.rpc.core.holder.RpcClientHolder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 19:33
 * @Description: 远程客户端的一些公共方法
 */
public class RemotingClientUtils {

    /**
     * 获取远程客户端
     *
     * @param rpcConfiguration RPC相关配
     * @param url              URL地址
     * @return 远程客户端实例
     */
    public static RemotingClient getRpcClient(RpcClientConfiguration rpcConfiguration, URL url) {
        return RpcClientHolder.computeIfAbsent(url.getProtocol(), url.getAddress(), (p, a) -> {
            String host = url.getHost();
            if (StringUtils.isBlank(host)) {
                throw new RpcException("客户端启动失败，必须指定host");
            }
            Integer port = url.getPort();
            int workThreads = rpcConfiguration.getWorkerThread();
            String codecProtocol = rpcConfiguration.getOrDefault(rpcConfiguration.getCodec(), CommonConstant.DEFAULT_CODEC);
            KeyValue<RemotingDataEncoder, RemotingDataDecoder> codec = CodecFactory.getCodec(codecProtocol);
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
            RemotingDataProcessor remotingDataProcessor = new RequestResponseRemotingDataProcessor(new SimpleHeartbeatRpcInvocation());
            conf.setRemotingDataProcessor(remotingDataProcessor);
            String client = rpcConfiguration.getProtocol();
            RemotingClient c = RemotingClientFactory.create(client);
            // 启动客户端
            if (c != null) {
                // 设置权重
                c.setWeight(url.getParameter(CommonConstant.WEIGHT_KEY, CommonConstant.DEFAULT_WEIGHT));

                c.start(conf);
            } else {
                throw new RpcRemotingException("无法创建对应的 远程客户端 ， client=" + client);
            }
            return c;
        });
    }
}
