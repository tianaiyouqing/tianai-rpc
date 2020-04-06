package cloud.tianai.rpc.core.factory;

import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RandomLoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RoundRobinLoadBalance;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 18:18
 * @Description: 负载均衡器工厂
 */
public class LoadBalanceFactory {

    private static Map<String, LoadBalance> loadBalanceMap = new ConcurrentHashMap<>(2);

    public static LoadBalance getLoadBalance(String name) {
        return loadBalanceMap.get(name);
    }

    public static void addLoadBalance(LoadBalance loadBalance) {
        loadBalanceMap.remove(loadBalance.getName());
        loadBalanceMap.put(loadBalance.getName(), loadBalance);
    }

    public static void addLoadBalance(String name, LoadBalance loadBalance) {
        loadBalanceMap.remove(name);
        loadBalanceMap.put(name, loadBalance);
    }

    static {
        // 加载 SPI
        ExtensionLoader<LoadBalance> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);
        Map<String, Class<? extends LoadBalance>> extensionClasses = extensionLoader.getExtensionClasses();
        Set<String> keys = extensionClasses.keySet();
        for (String key : keys) {
            LoadBalance extension = extensionLoader.getExtension(key);
            addLoadBalance(key, extension);
        }

        // ---------------------增加内置策略---------------------

        // 添加轮询策略
        addLoadBalance(new RoundRobinLoadBalance());
        // 添加随机策略
        addLoadBalance(new RandomLoadBalance());
    }

}
