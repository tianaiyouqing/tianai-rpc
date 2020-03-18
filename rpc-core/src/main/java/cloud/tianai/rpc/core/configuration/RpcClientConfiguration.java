package cloud.tianai.rpc.core.configuration;


import cloud.tianai.rpc.common.configuration.RpcConfiguration;
import cloud.tianai.rpc.common.sort.OrderComparator;
import cloud.tianai.rpc.core.context.RpcContextClientPostProcessor;
import cloud.tianai.rpc.core.template.RpcClientPostProcessor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:48
 * @Description: RPC客户端相关配置
 */
@Data
public class RpcClientConfiguration extends RpcConfiguration {

    private static final List<RpcClientPostProcessor> commonRpcClientPostProcessors = new LinkedList<>();

    private String loadBalance;

    private Integer requestTimeout = 3000;

    private Integer retry = 3;

    private boolean lazyLoadRegistry = true;
    private boolean lazyStartRpcClient = true;
    private List<RpcClientPostProcessor> rpcClientPostProcessors = new LinkedList<>();

    public void addRpcClientPostProcessor(RpcClientPostProcessor postProcessor) {
        assert postProcessor != null;
        removeRpcClientPostProcessor(postProcessor);
        rpcClientPostProcessors.add(postProcessor);
    }


    public List<RpcClientPostProcessor> getRpcClientPostProcessors() {
        List<RpcClientPostProcessor> result = new ArrayList<>(getRpcClientPostProcessorCount());
        // 先添加公共的
        result.addAll(commonRpcClientPostProcessors);
        // 再添加自定义的
        result.addAll(rpcClientPostProcessors);
        // 排序
        result.sort(OrderComparator.INSTANCE);
        return result;
    }

    public boolean removeRpcClientPostProcessor(RpcClientPostProcessor rpcClientPostProcessor) {
        if (!rpcClientPostProcessors.remove(rpcClientPostProcessor)) {
            return removeCommonRpcClientPostProcessor(rpcClientPostProcessor);
        }
        return true;
    }

    public int getRpcClientPostProcessorCount() {
        return commonRpcClientPostProcessors.size() + rpcClientPostProcessors.size();
    }

    public static boolean removeCommonRpcClientPostProcessor(RpcClientPostProcessor rpcClientPostProcessor) {
        return commonRpcClientPostProcessors.remove(rpcClientPostProcessor);
    }

    public static void addCommonRpcClientPostProcessor(RpcClientPostProcessor rpcClientPostProcessor) {
        assert rpcClientPostProcessor != null;
        removeCommonRpcClientPostProcessor(rpcClientPostProcessor);
        commonRpcClientPostProcessors.add(rpcClientPostProcessor);
    }

    static {
        // 添加一些默认解析器
        addCommonRpcClientPostProcessor(new RpcContextClientPostProcessor());
    }
}
