package cloud.tianai.rpc.core.util;

import cloud.tianai.rpc.remoting.api.*;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.core.holder.RpcClientHolder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;
import org.apache.commons.lang3.StringUtils;

import static cloud.tianai.rpc.common.constant.CommonConstant.*;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 19:33
 * @Description: 远程客户端的一些公共方法
 */
public class RemotingClientUtils {
    private static final RemotingDataProcessor REMOTING_DATA_PROCESSOR =
            new RequestResponseRemotingDataProcessor(new SimpleHeartbeatRpcInvocation());

    /**
     * 获取远程客户端
     *
     * @param url URL地址
     * @return 远程客户端实例
     */
    public static RemotingClient getRpcClient(URL url) {
        return RpcClientHolder.computeIfAbsent(url.getProtocol(), url.getAddress(), (p, a) -> {
            String protocol = url.getProtocol();
            if (RPC_PROXY_PROTOCOL.equals(protocol)) {
                // 默认使用 netty
                protocol = DEFAULT_REMOTING_PROTOCOL;
            }
            // 加载扩展
            ExtensionLoader<RemotingClient> extensionLoader = ExtensionLoader.getExtensionLoader(RemotingClient.class);
            RemotingClient c = extensionLoader.createExtension(protocol, false);
            // 启动客户端
            if (c != null) {
                c.start(url, REMOTING_DATA_PROCESSOR);
            } else {
                throw new RpcRemotingException("无法创建对应的 远程客户端 ， client=" + protocol);
            }
            return c;
        });
    }
}
