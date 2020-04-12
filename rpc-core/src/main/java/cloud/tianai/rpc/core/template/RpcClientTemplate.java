package cloud.tianai.rpc.core.template;

import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.remoting.api.Response;
import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
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
     *
     * @param request 请求数据
     * @param timeout 请求超时
     * @param retry   重试次数
     * @throws TimeoutException 超时异常
     * @return 返回数据
     */
    Response request(Request request, Integer timeout, Integer retry) throws TimeoutException;


    /**
     * RPC请求
     *
     * @param request      请求体
     * @param timeout      超时
     * @param connectRetry 连接重试
     * @param requestRetry 请求重试
     * @return Response
     * @throws TimeoutException
     */
    Response request(Request request, Integer timeout, Integer connectRetry, Integer requestRetry) throws TimeoutException;

    /**
     * 添加一个处理器
     * @param postProcessor 后处理器
     */
    void addPostProcessor(RpcClientPostProcessor postProcessor);

    /**
     * 获取服务注册
     *
     * @return
     */
    Registry getRegistry();

    /**
     * 获取当前执行的LoadBalance
     *
     * @return
     */
    LoadBalance getLoadBalance();

    /**
     * 获取RpcClient相关配置
     *
     * @return
     */
    RpcClientConfiguration getConfig();


    /**
     * 获取LOCK
     *
     * @return
     */
    Object getClientLock();

    /**
     * 获取URL
     *
     * @return
     */
    URL getUrl();

    /**
     * 获取远程客户端
     *
     * @return
     */
    List<RemotingClient> getRemotingClients();
}
