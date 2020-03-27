package cloud.tianai.remoting.api;

import java.util.Set;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 12:03
 * @Description: 远程数据解析
 */
public interface RemotingDataProcessor {

    /**
     * 读到数据， 回调函数
     * @param channel 具体的管道实现
     * @param msg 读到的数据
     * @param extend 扩展字段
     */
    void readMessage(Channel channel, Object msg, Object extend);


    /**
     * 写数据，回调函数， 用于扩展
     * @param channel 管道
     * @param msg 消息
     * @param extend 扩展字段
     * @return 如果不为空，则通过底层管道直接把数据写出去，如果为空，则不写数据
     */
    default Object writeMessage(Channel channel, Object msg, Object extend) {
        return msg;
    }

    /**
     * 发送心跳请求
     * @param channel
     * @param
     * @param extend
     */
    void sendHeartbeat(Channel channel, Object extend);

    /**
     * 发送异常
     * @param ex
     * @param data
     */
    void sendError(Channel channel, Throwable ex, Object data);

    /**
     * 该解析器是否支持给类型的数据解析
     * @param msg
     * @return
     */
    default boolean support(Object msg) {
        for (Class<?> supportParam : getSupportParams()) {
            if (supportParam.isInstance(msg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取支持解析的参数
     * @return
     */
    Class<?>[] getSupportParams();
}
