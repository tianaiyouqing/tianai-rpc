package cloud.tianai.rpc.core.template;

import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.holder.RegistryHolder;
import cloud.tianai.rpc.core.util.RegistryUtils;
import cloud.tianai.rpc.registory.api.NotifyListener;
import cloud.tianai.rpc.registory.api.Registry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 20:03
 * @Description: 抽象的RegistryRpcClentTemplate
 */
@Slf4j
public abstract class AbstractRegistryRpcClientTemplate extends AbstractRpcClientTemplate implements NotifyListener {

    /**
     * 服务注册.
     */
    private Registry registry;

    /**
     * 订阅的URLS.
     */
    protected volatile List<URL> subscribeUrls = Collections.emptyList();

    @Override
    public List<RemotingClient> getRemotingClients() {
        List<URL> urls = lookUpOfThrow();
        List<RemotingClient> remotingClients = new ArrayList<>(urls.size());
        for (URL url : urls) {
            RemotingClient remotingClient = createRpcClientIfNecessary(url);
            remotingClients.add(remotingClient);
        }
        return remotingClients;
    }

    @Override
    public Registry getRegistry() {
        if (registry == null) {
            initRegistryIfNecessary();
        }
        return registry;
    }

    protected void initRegistryIfNecessary() {
        URL registryUrl = getConfig().getRegistryUrl();
        this.registry = RegistryHolder.computeIfAbsent(registryUrl, (u) -> {
            Registry r1 = RegistryUtils.createAndStart(u);
            r1.subscribe(() -> {
                // 重新拉取
                getClientLock().lock();
                try {
                    lookAndSubscribeUrl(getUrl());
                } finally {
                    getClientLock().unlock();
                }
            });
            return r1;
        });
    }

    private List<URL> lookUpOfThrow() {
        if (CollectionUtils.isEmpty(subscribeUrls)) {
            getClientLock().lock();
            try {
                if (CollectionUtils.isEmpty(subscribeUrls)) {
                    lookAndSubscribeUrl(getUrl());
                }
            } finally {
                getClientLock().unlock();
            }
        }
        if (CollectionUtils.isEmpty(subscribeUrls)) {
            throw new RpcException("注册器中无法读取到该URL [" + getUrl() + "] 对应的注册地址");
        }
        return subscribeUrls;
    }

    protected void lookAndSubscribeUrl(URL url) {
        Result<List<URL>> lookup = getRegistry().lookup(url);
        if (!lookup.isSuccess()) {
            throw new RpcException("无法从registry中拉取配置消息, e=" + lookup.getMsg() + ", code=" + lookup.getCode());
        }
        List<URL> urls = lookup.getData();
        if (CollectionUtils.isEmpty(urls)) {
            // 没有读取到urls
            throw new RpcException("无法读取到对应接口的注册地址, " + url);
        }
        this.subscribeUrls = urls;
        log.info("TIAN-RPC REGISTRY {}拉取到消息:{}", getRegistry().getProtocol(), urls);
        getRegistry().subscribe(url, this);
    }

    @Override
    public void notify(List<URL> urls) {
        log.debug("[registry] 订阅到消息: {}", urls);
        this.subscribeUrls = urls;
    }
}
