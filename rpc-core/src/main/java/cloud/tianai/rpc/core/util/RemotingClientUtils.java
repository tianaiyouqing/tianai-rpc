package cloud.tianai.rpc.core.util;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.core.bootstrap.Bootstrap;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.core.holder.RpcClientHolder;
import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;

import java.util.HashMap;
import java.util.Map;

import static cloud.tianai.rpc.common.constant.CommonConstant.DEFAULT_REMOTING_PROTOCOL;
import static cloud.tianai.rpc.common.constant.CommonConstant.RPC_PROXY_PROTOCOL;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 19:33
 * @Description: 远程客户端的一些公共方法
 */
public class RemotingClientUtils {

    /**
     * 获取远程客户端
     *
     * @param url URL地址
     * @param config
     * @return 远程客户端实例
     */
    public static RemotingClient getRpcClient(URL url, RpcClientConfiguration config) {
        return RpcClientHolder.computeIfAbsent(config.getUrl().getProtocol(), url.getAddress(), (p, a) -> {
            // 使用config中配置的 protocol协议
            String protocol = config.getUrl().getProtocol();
            // 如果使用默认 tianai-rpc 协议，则使用默认远程客户端
            if (RPC_PROXY_PROTOCOL.equals(protocol)) {
                // 默认使用 netty
                protocol = DEFAULT_REMOTING_PROTOCOL;
            }
            // 加载扩展
            ExtensionLoader<RemotingClient> extensionLoader = ExtensionLoader.getExtensionLoader(RemotingClient.class);
            RemotingClient c = extensionLoader.createExtension(protocol, false);
            // 启动客户端
            if (c != null) {
                // 本地配置
                Map<String, String> parameters = new HashMap<>(config.getUrl().getParameters());
                Map<String, String> remotingParam = url.getParameters();
                parameters.putAll(remotingParam);

                c.start(url, Bootstrap.simpleRequestResponseRemotingDataProcessor(), parameters);
            } else {
                throw new RpcRemotingException("无法创建对应的 远程客户端 ， client=" + protocol);
            }
            return c;
        });
    }
}
