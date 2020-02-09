package cloud.tianai.rpc.core.template;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.registory.api.Registry;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 18:53
 * @Description: RPC 客户端模板
 */
public interface RpcClientTemplate {

    /**
     * RPC请求
     * @param request 请求数据
     * @param timeout 请求超时
     * @param retry 重试次数
     * @return 返回数据
     */
    Object request(Request request, Integer timeout, Integer retry) throws TimeoutException ;


    Object request(Request request, Integer timeout, Integer connectRetry, Integer requestRetry) throws TimeoutException;


    /**
     * 获取服务注册
     * @return
     */
    Registry getRegistry();

    /**
     * 获取当前执行的LoadBalance
     * @return
     */
    LoadBalance getLoadBalance();

    /**
     * 获取RpcClient相关配置
     * @return
     */
    RpcClientConfiguration getConfig();


    Object getClientLock();


    URL getUrl();

    List<RemotingClient> getRemotingClients();
}
