package cloud.tianai.remoting.api;


import java.net.SocketAddress;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 21:37
 * @Description: 远程管道
 */
public interface Channel {

    /**
     * 写数据
     * @param obj
     */
    void write(Object obj);

    /**
     * 获取本地地址
     * @return
     */
    SocketAddress getLocalAddress();

    /**
     * 获取远程地址
     * @return
     */
    SocketAddress getRemoteAddress();

    /**
     * 获取对应的真实的channel类， 比如Netty的channel
     * @return
     */
    Object getUnsafe();
}
