package cloud.tianai.rpc.core.template;

import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RandomLoadBalance;

import java.util.Optional;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 19:47
 * @Description: 抽象的RpcClientTemplate ， LoadBalance的相关实现
 */
public abstract class AbstractLoadBalanceRpcClientTemplate extends AbstractRegistryRpcClientTemplate {

    private LoadBalance loadBalance;

    @Override
    protected RemotingClient selectRemotingClient(Request request) {
        return getLoadBalance().select(getRemotingClients(), request);
    }

    @Override
    public LoadBalance getLoadBalance() {
        if (loadBalance == null) {
            initLoadBalance();
        }
        return loadBalance;
    }

    protected void initLoadBalance() {
        if (loadBalance == null) {
            RpcClientConfiguration config = getConfig();
            String loadBalanceName = config.getOrDefault(config.getLoadBalance(), RandomLoadBalance.NAME);
            // 读取 负载均衡
            loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalanceName);
            if (loadBalance == null) {
                throw new RpcException("未找到对应的轮询策略, loadBalanceName=" + loadBalanceName);
            }
        }
    }
}
