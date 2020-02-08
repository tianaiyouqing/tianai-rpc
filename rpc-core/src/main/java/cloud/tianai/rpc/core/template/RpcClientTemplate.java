package cloud.tianai.rpc.core.template;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.exception.RpcChannelClosedException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/01 10:58
 * @Description: RpcClient客户端
 */
@Slf4j
public class RpcClientTemplate {
    private int requestTimeout;
    private int retry;
    private final Object lockFlag;

    public RpcClientTemplate(int requestTimeout, int retry, Object lockFlag) {
        this.requestTimeout = requestTimeout;
        this.retry = retry;
        this.lockFlag = lockFlag;
    }

    public RpcClientTemplate(int requestTimeout, int retry) {
        this.requestTimeout = requestTimeout;
        this.retry = retry;
        this.lockFlag = new Object();
    }


    public Object request(RemotingClient rpcClient, Request request) throws TimeoutException {
        // 通过负载均衡读取到对应的rpcClient
        // 如果请求超时，理应再从负载均衡器里拿一个连接执行重试
        CompletableFuture<Object> future = rpcClient.getchannel().request(request, requestTimeout);
        Object resObj = null;
        try {
            resObj = future.get(requestTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RpcChannelClosedException) {
                // 如果是管道关闭异常，进行重连试试
                reconnectIfNecessary(rpcClient, retry);
                // 如果连接成功,重新请求
                if (rpcClient.isActive()) {
                    resObj = request(rpcClient, request);
                } else {
                    throw new TimeoutException(e.getCause().getMessage());
                }
            } else {
                throw new TimeoutException(e.getCause().getMessage());
            }
        }
        return resObj;
    }

    private void reconnectIfNecessary(RemotingClient rpcClient, int retryCount) throws TimeoutException {
        if (!rpcClient.isActive()) {
            synchronized (lockFlag) {
                if (!rpcClient.isActive()) {
                    // 重新连接
                    rpcClient.reconnect(retryCount);
                }
            }
        }
    }

    public Object retryRequest(RemotingClient rpcClient, Request request, Function<Request, RemotingClient> loadBalance) throws TimeoutException {
        return retryRequest(rpcClient, request, 0, loadBalance);
    }

    private Object retryRequest(RemotingClient rpcClient, Request request, int currRetry, Function<Request, RemotingClient> loadBalance) throws TimeoutException {
        // 负责继续请求重试
        try {
            Object res = request(rpcClient, request);
            return res;
        } catch (TimeoutException e) {
            currRetry++;
            // 如果超过重试次数， 直接抛异常
            log.info("请求重试, 请求体 [{}], 当前已重试次数{}", request, currRetry);
            if (currRetry > retry) {
                throw new TimeoutException("请求失败， 超过最大重试次数");
            }
            // 休眠100毫秒重试一下
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {
                // 不做处理
            }
            log.info("请求重试, 请求体 [{}], 当前已重试次数{}", request, currRetry);
            return retryRequest(loadBalance.apply(request), request, currRetry, loadBalance);
        }
    }
}
