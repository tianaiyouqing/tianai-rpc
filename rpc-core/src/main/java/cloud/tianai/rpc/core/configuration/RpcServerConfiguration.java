package cloud.tianai.rpc.core.configuration;


import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.common.configuration.RpcConfiguration;
import cloud.tianai.rpc.common.sort.OrderComparator;
import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.context.RpcContextInvocationPostProcessor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:50
 * @Description: RPC Server 配置
 */
@Data
public class RpcServerConfiguration extends RpcConfiguration {

    private static final List<RpcInvocationPostProcessor> RPC_INVOCATION_POST_PROCESSORS = new LinkedList<>();

    /**
     * Host 地址.
     */
    private String host = IPUtils.getHostIp();

    /**
     * 端口.
     */
    private Integer port = 20881;

    /**
     * boss线程.
     */
    private Integer bossThreads = 1;
    /**
     * 心跳时间，推荐使用默认的.
     */
    private Integer idleTimeout;
    private List<RpcInvocationPostProcessor> invocationPostProcessors = new LinkedList<>();

    public void addRpcInvocationPostProcessor(RpcInvocationPostProcessor rpcInvocationPostProcessor) {
        removeInvocationPostProcessor(rpcInvocationPostProcessor);
        invocationPostProcessors.add(rpcInvocationPostProcessor);
    }

    public static void addCommonRpcInvocationPostProcessor(RpcInvocationPostProcessor commonRpcInvocationPostProcessor) {
        removeCommonRpcInvocationPostProcessor(commonRpcInvocationPostProcessor);
        RPC_INVOCATION_POST_PROCESSORS.add(commonRpcInvocationPostProcessor);
    }

    public static boolean removeCommonRpcInvocationPostProcessor(RpcInvocationPostProcessor commonRpcInvocationPostProcessor) {
        return RPC_INVOCATION_POST_PROCESSORS.remove(commonRpcInvocationPostProcessor);
    }

    public List<RpcInvocationPostProcessor> getInvocationPostProcessors() {
        List<RpcInvocationPostProcessor> result = new ArrayList<>(getInvocationPostProcessorCount());
        // 先添加公共的
        result.addAll(RPC_INVOCATION_POST_PROCESSORS);
        // 再添加自定义的
        result.addAll(invocationPostProcessors);
        // 排序
        result.sort(OrderComparator.INSTANCE);
        return result;
    }


    public int getInvocationPostProcessorCount() {
        return invocationPostProcessors.size() + RPC_INVOCATION_POST_PROCESSORS.size();
    }

    public boolean removeInvocationPostProcessor(RpcInvocationPostProcessor rpcInvocationPostProcessor) {
        if (!invocationPostProcessors.remove(rpcInvocationPostProcessor)) {
            return removeCommonRpcInvocationPostProcessor(rpcInvocationPostProcessor);
        }
        return true;
    }

    static {
        // 添加一些默认解析器
        addCommonRpcInvocationPostProcessor(new RpcContextInvocationPostProcessor());
    }
}
