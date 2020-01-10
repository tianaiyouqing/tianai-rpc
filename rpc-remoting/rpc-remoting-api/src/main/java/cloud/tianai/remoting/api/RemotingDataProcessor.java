package cloud.tianai.remoting.api;

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
    Object writeMessage(Channel channel, Object msg, Object extend);

    /**
     * 该解析器是否支持给类型的数据解析
     * @param msg
     * @return
     */
    boolean support(Object msg);
}
