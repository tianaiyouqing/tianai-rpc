package cloud.tianai.remoting.api;

import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/10 13:28
 * @Description: 远程客户端
 */
public interface RemotingClient extends RemotingEndpoint {

    /**
     * 连接
     */
    void doConnect();

    /**
     * 重新连接，
     * @param retryCount 重试次数
     * @throws TimeoutException 超时异常
     */
    void reconnect(int retryCount) throws TimeoutException;

    /**
     * 获取远程地址
     * @return
     */
    SocketAddress getRemoteAddress();
}
