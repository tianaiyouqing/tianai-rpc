package cloud.tianai.rpc.core.configuration;


import cloud.tianai.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.common.configuration.RpcConfiguration;
import cloud.tianai.rpc.common.util.IPUtils;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:50
 * @Description: RPC Server 配置
 */
@Data
public class RpcServerConfiguration extends RpcConfiguration {

    /** Host 地址. */
    private String host = IPUtils.getHostIp();

    /** 端口. */
    private Integer port = 20881;

    /** boss线程. */
    private Integer bossThreads = 1;
    /** 心跳时间，推荐使用默认的. */
    private Integer idleTimeout;
    private List<RpcInvocationPostProcessor> invocationPostProcessors = new LinkedList<>();

    public void addRpcInvocationPostProcessor(RpcInvocationPostProcessor rpcInvocationPostProcessor) {
        invocationPostProcessors.add(rpcInvocationPostProcessor);
    }
}
