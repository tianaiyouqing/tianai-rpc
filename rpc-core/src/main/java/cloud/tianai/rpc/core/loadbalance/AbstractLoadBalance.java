package cloud.tianai.rpc.core.loadbalance;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.util.CollectionUtils;

import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/10 14:02
 * @Description: 抽象的 负载均衡器
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public RemotingClient select(List<RemotingClient> rpcClients, Request request) {
        if (CollectionUtils.isEmpty(rpcClients)) {
            return null;
        }
        if (rpcClients.size() == 1) {
            return rpcClients.get(0);
        }
        return doSelect(rpcClients, request);
    }

    protected int getWeight(RemotingClient remotingClient) {
        int weight = remotingClient.getWeight();
        return Math.max(0, weight);
    }


    protected abstract RemotingClient doSelect(List<RemotingClient> rpcClients, Request request);
}
