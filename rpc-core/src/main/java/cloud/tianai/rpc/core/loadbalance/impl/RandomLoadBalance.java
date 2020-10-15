package cloud.tianai.rpc.core.loadbalance.impl;

import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.remoting.api.Request;
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
        // 判断权重是否都相同
        boolean sameWeight = true;
        // 总权重，用来计算随机权重
        int totalWeight= 0;

        for (int i = 0; i < size; i++) {
            int weight = getWeight(rpcClients.get(i));
            weights[i] = totalWeight;
            // Sum
            totalWeight += weight;
            if (sameWeight && totalWeight != weight * (i + 1)) {
                sameWeight = false;
            }
        }
        // 通过权重进行随机
        if(totalWeight > 0 && !sameWeight) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int i = 0; i < size; i++) {
                // 设置的权重越大减为0的几率也就越大，概率也就越大
                if(offset < weights[i]) {
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


    public static void main(String[] args) {
        System.out.println(RemotingClient.class);
    }
}
