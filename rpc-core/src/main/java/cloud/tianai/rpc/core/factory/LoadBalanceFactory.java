package cloud.tianai.rpc.core.factory;

import cloud.tianai.rpc.core.loadbalance.LoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RoundRobinLoadBalance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadBalanceFactory {

    private static Map<String, LoadBalance> loadBalanceMap = new ConcurrentHashMap<>(2);

    public static LoadBalance getLoadBalance(String name) {
        return loadBalanceMap.get(name);
    }

    public static void addLoadBalance(LoadBalance loadBalance) {
        loadBalanceMap.put(loadBalance.getName(), loadBalance);
    }


    static {
        // 添加轮询策略
        addLoadBalance(new RoundRobinLoadBalance());
    }

}
