package cloud.tianai.rpc.core.loadbalance;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.URL;

import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 11:34
 * @Description: 负载均衡器
 */
public interface LoadBalance {

    /**
     * 该负载均衡器的名称
     * @return
     */
    String getName();

    /**
     * 负载具体实现
     * @param rpcClients
     * @param request
     * @return
     */
    RemotingClient select(List<RemotingClient> rpcClients, Request request);
}
