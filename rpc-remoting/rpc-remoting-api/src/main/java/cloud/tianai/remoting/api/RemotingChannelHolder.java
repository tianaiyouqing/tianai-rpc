package cloud.tianai.remoting.api;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 11:58
 * @Description: 远程管道持有者
 */
public interface RemotingChannelHolder {

    /**
     * 获取当前管道类型
     * @return
     */
    String getChannelType();

    /**
     * 获取管道
     * @return 具体的管道实现
     */
    Channel getNettyChannel();

    /**
     * 发送消息
     * @param request 消息体
     * @param timeout 超时时间
     * @return
     */
    CompletableFuture<Object> request(Request request, int timeout);
}
