package cloud.tianai.rpc.core.template;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.core.factory.LoadBalanceFactory;
import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RandomLoadBalance;

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
            loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceName);
            if (loadBalance == null) {
                throw new RpcException("未找到对应的轮询策略, loadBalanceName=" + loadBalanceName);
            }
        }
    }
}
