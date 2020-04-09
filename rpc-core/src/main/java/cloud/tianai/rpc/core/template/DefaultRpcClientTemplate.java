package cloud.tianai.rpc.core.template;

import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.common.URL;

import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/09 20:09
 * @Description: 默认的RpcClient模板
 */
public class DefaultRpcClientTemplate extends AbstractLoadBalanceRpcClientTemplate{

    private RpcClientConfiguration rpcClientConfiguration;
    private URL url;

    public DefaultRpcClientTemplate(RpcClientConfiguration rpcClientConfiguration, URL url) {
        this(rpcClientConfiguration, url, true, false, true);
    }

    public DefaultRpcClientTemplate(RpcClientConfiguration rpcClientConfiguration,
                                    URL url,
                                    boolean lazyLoadRegistry,
                                    boolean lazyLoadRemotingRpcClient,
                                    boolean lazyLoadLoadBalance) {
        this.rpcClientConfiguration = rpcClientConfiguration;
        this.url = url;
        // 添加后处理器
        List<RpcClientPostProcessor> postProcessors = rpcClientConfiguration.getRpcClientPostProcessors();
        if(CollectionUtils.isNotEmpty(postProcessors)){
            postProcessors.forEach(this::addPostProcessor);
        }
        if(!lazyLoadLoadBalance) {
            initLoadBalance();
        }
        if(!lazyLoadRegistry) {
            initRegistryIfNecessary();
        }
        if(!lazyLoadRemotingRpcClient) {
            getRemotingClients();
        }

    }


    @Override
    public RpcClientConfiguration getConfig() {
        return rpcClientConfiguration;
    }

    @Override
    public URL getUrl() {
        return url;
    }
}
