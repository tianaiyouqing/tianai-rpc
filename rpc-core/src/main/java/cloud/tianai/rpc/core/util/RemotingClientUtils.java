package cloud.tianai.rpc.core.util;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.RemotingConfiguration;
import cloud.tianai.remoting.api.RequestResponseRemotingDataProcessor;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.client.proxy.AbstractRpcProxy;
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
     * @param rpcConfiguration RPC相关配
     * @param url URL地址
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
            conf.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(new AbstractRpcProxy.HeartbeatRpcInvocation()));
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
}
