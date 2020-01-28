package cloud.tianai.rpc.core.loadbalance;

import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.client.RpcClient;

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
     * @param url
     * @param request
     * @return
     */
    RpcClient select(List<RpcClient> rpcClients, URL url, Request request);
}
