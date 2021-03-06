package cloud.tianai.rpc.registory.api;

import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.registory.api.exception.RpcRegistryException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/04 19:52
 * @Description: 抽象的registry
 */
@Slf4j
@Getter
public abstract class AbstractRegistry implements Registry {
    public static final int DEFAULT_RETRY_COUNT = 30;

    private AtomicBoolean start = new AtomicBoolean(false);
    private Map<String, URL> registryUrlMap = new ConcurrentHashMap<>(16);
    private Set<StatusListener> statusListenerSet = new CopyOnWriteArraySet<>();
    private URL registryUrl;

    /**
     * 重试次数.
     */
    private Integer retryCount;

    @Override
    public Result<?> register(URL url) {
        doRegister(url, true);
        return Result.ofSuccess("register success");
    }

    @Override
    public URL getUrl() {
        return registryUrl;
    }

    protected void doRegister(URL url, boolean cache) {
        innerRegister(url);
        if (cache) {
            registryUrlMap.remove(url.getAddress());
            registryUrlMap.put(url.getAddress(), url);
        }
        log.info("TIANAI-RPC REGISTRY {} 地址注册: {}", getProtocol(), url);
    }

    protected void reRegister() {
        for (URL url : registryUrlMap.values()) {
            doRegister(url, false);
        }
    }

    @Override
    public boolean isStart() {
        return start.get();
    }


    @Override
    public void subscribe(StatusListener statusListener) {
        statusListenerSet.add(statusListener);
        doSubscribe(statusListener);
    }


    @Override
    public Registry start(URL url) {
        if (start.compareAndSet(false, true)) {
            // 设置重试次数
            retryCount = url.getParameter(CommonConstant.RETRY, DEFAULT_RETRY_COUNT);
            registryUrl = url;
            try {
                doStart(url);
            } catch (TimeoutException e) {
                // 启动失败, 执行shutdown
                destroy();
                throw new RpcRegistryException(e.getMessage(), e);
            }
        } else {
            throw new RpcRegistryException("该Registry已启动，不可重复启动");
        }
        return this;
    }


    @Override
    public void destroy() {
        if (start.compareAndSet(true, false)) {
            doDestroy();
            registryUrlMap.clear();
            statusListenerSet.clear();
        }
    }

    /**
     * 停止
     */
    protected abstract void doDestroy();

    /**
     * 子类实现，具体的start方法
     * @param url URL参数
     * @throws TimeoutException 可能会抛出超时异常
     */
    protected abstract void doStart(URL url) throws TimeoutException;

    /**
     * 内部实现的具体 服务注册机制
     * @param url 待注册的URL
     */
    protected abstract void innerRegister(URL url);

    /**
     * 订阅服务注册的监听器
     * @param statusListener 状态监听器
     */
    protected abstract void doSubscribe(StatusListener statusListener);
}
