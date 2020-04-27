package cloud.tianai.rpc.core.bootstrap;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.core.holder.RpcClientHolder;
import cloud.tianai.rpc.remoting.api.*;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/27 21:54
 * @Description: 客户端脚手架工具
 */
public class Bootstrap {
    /**
     * 是否启动.
     */
    private AtomicBoolean start = new AtomicBoolean(false);
    /**
     * 远程客户端.
     */
    private RemotingClient remotingClient;
    /**
     * 对应的协议.
     */
    private String protocol = "netty";
    /**
     * 当前URL.
     */
    private URL url;
    @Getter
    private RemotingConfiguration conf = new RemotingConfiguration();
    private String client = "netty";
    private String codec = "hessian2";


    /**
     * 简单的 RequestResponse 远程解析器实例
     */
    public static class SimpleRequestResponseRemotingDataProcessor {
        public static final RemotingDataProcessor INSTANCE =
                new RequestResponseRemotingDataProcessor(new SimpleHeartbeatRpcInvocation());
    }

    public static RemotingDataProcessor simpleRequestResponseRemotingDataProcessor() {
        return SimpleRequestResponseRemotingDataProcessor.INSTANCE;
    }

    public Bootstrap codec(String codec) {
        this.codec = codec;
        return this;
    }

    private Bootstrap timeout(Integer timeout) {
        conf.setConnectTimeout(timeout);
        return this;
    }

    public Bootstrap client(String client) {
        this.client = client;
        return this;
    }

    public RemotingClient start(URL url) throws RpcException {
        if (start.compareAndSet(false, true)) {
            try {
                this.url = url;
                // 启动远程链接
                startRemotingClient();
            } finally {
                start.set(false);
            }
        }
        return remotingClient;
    }

    private void startRemotingClient() {
        remotingClient = RpcClientHolder.computeIfAbsent(protocol, url.getAddress(), (p, u) -> {
            // 加载远程客户端
            RemotingClient r = ExtensionLoader
                    .getExtensionLoader(RemotingClient.class)
                    .createExtension(protocol, false);
            if (Objects.isNull(r)) {
                throw new RpcException("未找到对应的远程server, protocol=" + protocol);
            }
            r.start(url, simpleRequestResponseRemotingDataProcessor(), url.getParameters());
            return r;
        });
    }


    public void shutdown() {
        if (start.compareAndSet(true, false)) {
            if (remotingClient != null) {
                remotingClient.destroy();
            }
        }
    }

    public boolean isStart() {
        return start.get();
    }
}
