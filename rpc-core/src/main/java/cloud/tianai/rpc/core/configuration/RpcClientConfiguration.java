package cloud.tianai.rpc.core.configuration;


import cloud.tianai.rpc.common.configuration.RpcConfiguration;
import cloud.tianai.rpc.core.template.RpcClientPostProcessor;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:48
 * @Description: RPC客户端相关配置
 */
@Data
public class RpcClientConfiguration extends RpcConfiguration {

    private String loadBalance;

    private Integer requestTimeout = 3000;

    private Integer retry = 3;

    private boolean lazyLoadRegistry = true;
    private boolean lazyStartRpcClient = true;
    private List<RpcClientPostProcessor> rpcClientPostProcessors = new LinkedList<>();

    public void addRpcClientPostProcessor(RpcClientPostProcessor postProcessor) {
        rpcClientPostProcessors.add(postProcessor);
    }
}
