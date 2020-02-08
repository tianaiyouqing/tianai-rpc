package cloud.tianai.remoting.api;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:52
 * @Description: future
 */
@Slf4j
@Data
public class DefaultFuture extends CompletableFuture<Object> {

    private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<>();
    private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<>();


    private ExecutorService executor;
    private final Long id;
    private final Channel channel;
    private final Request request;
    private final int timeout;
    private final long start = System.currentTimeMillis();
    private volatile long sent;

    private DefaultFuture(Channel channel, Request request, int timeout) {
        this.channel = channel;
        this.request = request;
        this.id = request.getId();
        this.timeout = timeout;
        // put into waiting map.
        FUTURES.put(id, this);
        CHANNELS.put(id, channel);
    }

    public static void received(Channel channel, Response response, boolean timeout) {
        try {
            DefaultFuture future = FUTURES.remove(response.getId());
            if (future != null) {
                future.doReceived(response);
            } else if (response.isHeartbeat()) {
                // 如果是心跳请求返回的数据， 直接不做处理
                log.debug("远程地址[" + channel.getRemoteAddress() +"]  -> 心跳请求返回.");
            } else {
                log.warn("The timeout response finally returned at "
                        + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
                        + ", response " + response
                        + (channel == null ? "" : ", channel: " + channel.getLocalAddress()
                        + " -> " + channel.getRemoteAddress()));
            }
        } finally {
            CHANNELS.remove(response.getId());
        }
    }

    private void doReceived(Response res) {
        if (res == null) {
            throw new IllegalStateException("response cannot be null");
        }
        this.complete(res);
    }

    public static DefaultFuture newFuture(Channel channel, Request request, int timeout) {
        final DefaultFuture future = new DefaultFuture(channel, request, timeout);
        return future;
    }

    public static DefaultFuture newFuture(Channel channel, Request request, int timeout, ExecutorService executor) {
        final DefaultFuture future = new DefaultFuture(channel, request, timeout);
        future.setExecutor(executor);
        return future;
    }
}
