package cloud.tianai.remoting.api;

public interface RemotingDataInterceptor {

    /**
     * 读到数据， 回调函数
     * @param channel 具体的管道实现
     * @param msg 读到的数据
     * @param extend 扩展字段
     */
    default Object beforeReadMessage(Channel channel, Object msg, Object extend) {
        return msg;
    }


    /**
     * 在写数据之前调用
     * @param channel
     * @param msg
     * @param extend
     * @return
     */
    default Object beforeWriteMessage(Channel channel, Object msg, Object extend) {
        return msg;
    }
}
