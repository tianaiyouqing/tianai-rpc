package cloud.tianai.rpc.common;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/25 21:54
 * @Description: Node API
 */
public interface Node {

    /**
     * get url.
     *
     * @return url.
     */
    URL getUrl();

    /**
     * 是否处于活跃状态，并已经连接
     * @return
     */
    boolean isActive();


    /**
     * 销毁
     */
    void destroy();

}