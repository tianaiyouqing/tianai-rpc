package cloud.tianai.rpc.core.configuration;


import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.configuration.RpcConfiguration;
import cloud.tianai.rpc.common.sort.OrderComparator;
import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.context.RpcContextClientPostProcessor;
import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cloud.tianai.rpc.common.constant.CommonConstant.CODEC_KEY;
import static cloud.tianai.rpc.common.constant.CommonConstant.RPC_PROXY_PROTOCOL;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:48
 * @Description: RPC客户端相关配置
 */
@Data
public class RpcClientConfiguration {

    private static final List<RpcClientPostProcessor> RPC_CLIENT_POST_PROCESSORS = new LinkedList<>();

    private String loadBalance;

    private Integer requestTimeout = 3000;

    private Integer retry = 3;

    /** 服务注册地址. */
    private URL registryUrl;

    private URL url = new URL();

    /** 超时. */
    private Integer timeout = 5000;


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
        result.addAll(RPC_CLIENT_POST_PROCESSORS);
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
        return RPC_CLIENT_POST_PROCESSORS.size() + rpcClientPostProcessors.size();
    }

    public static boolean removeCommonRpcClientPostProcessor(RpcClientPostProcessor rpcClientPostProcessor) {
        return RPC_CLIENT_POST_PROCESSORS.remove(rpcClientPostProcessor);
    }

    public static void addCommonRpcClientPostProcessor(RpcClientPostProcessor rpcClientPostProcessor) {
        assert rpcClientPostProcessor != null;
        removeCommonRpcClientPostProcessor(rpcClientPostProcessor);
        RPC_CLIENT_POST_PROCESSORS.add(rpcClientPostProcessor);
    }

    static {
        // 添加一些默认解析器
        addCommonRpcClientPostProcessor(new RpcContextClientPostProcessor());
    }

    public <T> T getOrDefault(T val, T defVal) {
        if (val == null) {
            return defVal;
        }
        return val;
    }

    public void setCodec(String codec) {
        addParameter(CODEC_KEY, codec);
    }

    public void addParameter(String key, Object value) {
        setUrl(getUrl().addParameter(key, value));
    }
    public void setProtocol(String protocol) {
        getUrl().setProtocol(protocol);
    }
}
