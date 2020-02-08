package cloud.tianai.rpc.registory.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 15:05
 * @Description: 状态监听器
 */
public interface StatusListener {

    /**
     * 重新连接事件
     */
    void reConnected();
}
