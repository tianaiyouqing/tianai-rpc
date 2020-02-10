package cloud.tianai.rpc.core.loadbalance.impl;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.core.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 11:34
 * @Description: 轮询模式的负载均衡器
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "roundrobin";

    private static final int RECYCLE_PERIOD = 60000;
    private ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<String, ConcurrentMap<String, WeightedRoundRobin>>();
    private AtomicBoolean updateLock = new AtomicBoolean();


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected RemotingClient doSelect(List<RemotingClient> rpcClients, Request request) {
        String key = request.getInterfaceType().getName() + "." + request.getMethodName();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.get(key);
        if (map == null) {
            methodWeightMap.putIfAbsent(key, new ConcurrentHashMap<String, WeightedRoundRobin>(16));
            map = methodWeightMap.get(key);
        }

        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        RemotingClient selectedRpcClient = null;
        WeightedRoundRobin selectedWrr = null;

        for (RemotingClient rpcClient : rpcClients) {
            String id = rpcClient.getId();
            WeightedRoundRobin weightedRoundRobin = map.get(id);
            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin();
                weightedRoundRobin.setWeight(getWeight(rpcClient));
                map.putIfAbsent(id, weightedRoundRobin);
            }
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedRpcClient = rpcClient;
                selectedWrr = weightedRoundRobin;
            }
            totalWeight += weightedRoundRobin.getWeight();
        }

        // 清除一些长时间不使用的 WeightedRoundRobin
        if (!updateLock.get() && rpcClients.size() != map.size()) {
            if (updateLock.compareAndSet(false, true)) {
                try {
                    // copy -> modify -> update reference
                    ConcurrentMap<String, WeightedRoundRobin> newMap = new ConcurrentHashMap<>(map);
                    newMap.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > RECYCLE_PERIOD);
                    methodWeightMap.put(key, newMap);
                } finally {
                    updateLock.set(false);
                }
            }
        }
        if (selectedRpcClient != null) {
            selectedWrr.sel(totalWeight);
            return selectedRpcClient;
        }

        return rpcClients.get(0);
    }


    protected static class WeightedRoundRobin {
        private int weight;
        private AtomicLong current = new AtomicLong(0);
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            current.set(0);
        }

        public long increaseCurrent() {
            return current.addAndGet(weight);
        }

        public void sel(int total) {
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }


}
