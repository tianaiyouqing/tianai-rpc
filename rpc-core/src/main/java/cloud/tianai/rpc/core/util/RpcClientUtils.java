package cloud.tianai.rpc.core.util;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.exception.RpcChannelClosedException;
import cloud.tianai.remoting.api.exception.RpcRemotingException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/01 11:19
 * @Description: rpcClient 工具类
 */
public class RpcClientUtils {

    /**
     * 重新连接， 相比RpcClient自带的重新连接，这个方法增加了重试次数
     * @param rpcClient 要重新连接的rpcClient
     * @param currRetryCount 当前重试的次数
     * @param retryCount 重试次数
     * @throws TimeoutException 超过重试次数还没连上，报超时异常
     */
    public static void reconnect(RemotingClient rpcClient, int currRetryCount, int retryCount) throws TimeoutException {
        try {
            rpcClient.doConnect();
        } catch (RpcRemotingException ex) {
            // 休眠100毫秒
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                // 不做处理
            }
            // 这里应该进行重试
            if (currRetryCount < retryCount) {
                reconnect(rpcClient, ++currRetryCount, retryCount);
            } else {
                throw new TimeoutException(ex.getMessage());
            }
        }
    }
}
