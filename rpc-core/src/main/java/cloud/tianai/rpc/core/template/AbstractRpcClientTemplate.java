package cloud.tianai.rpc.core.template;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.exception.RpcChannelClosedException;
import cloud.tianai.rpc.common.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.util.RemotingClientUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 19:28
 * @Description: 抽象的RpcClientTemplate， 该抽象方法封装了重试的公共逻辑
 */
@Slf4j
public abstract class AbstractRpcClientTemplate implements RpcClientTemplate {

    private final Object lock = new Object();

    @Override
    public Object request(Request request, Integer timeout, Integer retry) throws TimeoutException {
        return request(request, timeout, retry, retry);
    }

    @Override
    public Object request(Request request, Integer timeout, Integer connectRetry, Integer requestRetry) throws TimeoutException {
        return retryRequest(request, 0, connectRetry, requestRetry);
    }

    /**
     * 重试请求
     *
     * @param request      请求数
     * @param currRetry    当前已经重试的次数，
     * @param connectRetry 连接重试数
     * @param requestRetry 请求重试数
     * @return RPC 远程返回的数据
     * @throws TimeoutException 超时异常
     */
    public Object retryRequest(Request request, int currRetry, final Integer connectRetry, final Integer requestRetry) throws TimeoutException {
        RemotingClient remotingClient = selectRemotingClient(request);
        try {
            Object res = request(remotingClient, request, connectRetry);
            return res;
        } catch (TimeoutException e) {
            currRetry++;
            // 如果超过重试次数， 直接抛异常
            log.info("请求重试, 请求体 [{}], 当前已重试次数{}", request, currRetry);
            if (currRetry > requestRetry) {
                throw new TimeoutException("请求失败， 超过最大重试次数");
            }
            // 休眠100毫秒重试一下
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {
                // 不做处理
            }
            log.info("请求重试, 请求体 [{}], 当前已重试次数{}", request, currRetry);
            return retryRequest(request, currRetry, connectRetry, requestRetry);
        }
    }


    /**
     * 请求数据
     *
     * @param remotingClient 远程客户端
     * @param request        请求数据
     * @param connectRetry   连接重试(连接断开时重连次数)
     * @return 请求返回数据
     * @throws TimeoutException 超时异常
     */
    private Object request(RemotingClient remotingClient, Request request, Integer connectRetry) throws TimeoutException {
        // 通过负载均衡读取到对应的rpcClient
        // 如果请求超时，理应再从负载均衡器里拿一个连接执行重试
        CompletableFuture<Object> future = remotingClient.getChannel().request(request, getRequestTimeout());
        Object resObj = null;
        try {
            resObj = future.get(getRequestTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RpcChannelClosedException) {
                // 如果是管道关闭异常，进行重连试试
                reconnectIfNecessary(remotingClient, connectRetry);
                // 如果连接成功,重新请求
                if (remotingClient.isActive()) {
                    resObj = request(remotingClient, request, connectRetry);
                } else {
                    throw new TimeoutException(e.getCause().getMessage());
                }
            } else {
                throw new TimeoutException(e.getCause().getMessage());
            }
        }
        return resObj;
    }

    /**
     * 重新连接远程客户端
     *
     * @param remotingClient 远程客户端
     * @param connectRetry   连接重试次数
     * @throws TimeoutException 连接失败抛出超时异常
     */
    private void reconnectIfNecessary(RemotingClient remotingClient, Integer connectRetry) throws TimeoutException {
        if (!remotingClient.isActive()) {
            synchronized (getClientLock()) {
                if (!remotingClient.isActive()) {
                    // 重新连接
                    remotingClient.reconnect(connectRetry);
                }
            }
        }
    }

    /**
     * 获取请求Timeout
     *
     * @return
     */
    protected Integer getRequestTimeout() {
        Integer requestTimeout = getConfig().getRequestTimeout();
        return requestTimeout == null || requestTimeout < 1 ? 3000 : requestTimeout;
    }


    @Override
    public Object getClientLock() {
        return lock;
    }


    protected RemotingClient createRpcClientIfNecessary(URL url) {
        RpcClientConfiguration rpcConfiguration = getConfig();
        return RemotingClientUtils.getRpcClient(rpcConfiguration, url);
    }

    /**
     * 获取远程客户端
     *
     * @param request 请求数据
     * @return
     */
    protected abstract RemotingClient selectRemotingClient(Request request);
}
