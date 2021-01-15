package cloud.tianai.rpc.remoting.api;

/**
 * @Author: 天爱有情
 * @date 2021/1/15 16:44
 * @Description channel 持有者
 */
public class ChannelHolder {

    private static final ThreadLocal<Channel> CHANNEL_THREAD_LOCAL = new ThreadLocal<>();


    public static void bind(Channel channel) {
        CHANNEL_THREAD_LOCAL.set(channel);
    }

    public static void unBind() {
        CHANNEL_THREAD_LOCAL.remove();
    }

    public static Channel get() {
        return CHANNEL_THREAD_LOCAL.get();
    }
}
