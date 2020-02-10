package cloud.tianai.rpc.core.loadbalance.impl;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.core.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/10 14:17
 * @Description: 权重越重， 理论上概率越大. 如果权重小，那么随机到的概率越低
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "random";

    @Override
    protected RemotingClient doSelect(List<RemotingClient> rpcClients, Request request) {
        int size = rpcClients.size();
        int[] weights = new int[size];

        int firstWeight =getWeight(rpcClients.get(0));
        weights[0] = firstWeight;
        boolean sameWeight = true;
        // 总权重，用来计算随机权重
        int totalWeight= firstWeight;

        for (int i = 1; i < size; i++) {
            int weight = getWeight(rpcClients.get(i));
            weights[i] = weight;
            // Sum
            totalWeight += weight;
            if (sameWeight && weight != firstWeight) {
                // 设置相同权重为 false
                sameWeight = false;
            }
        }

        // 通过权重进行随机
        if(totalWeight != 0 && !sameWeight) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int i = 0; i < size; i++) {
                // 设置的权重越大减为0的几率也就越大，概率也就越大
                offset -= weights[i];
                if(offset < 0) {
                    return rpcClients.get(i);
                }
            }
        }
        // 如果权重都相同，直接随机取下标
        return rpcClients.get(ThreadLocalRandom.current().nextInt(size));
    }


    @Override
    public String getName() {
        return NAME;
    }
}
