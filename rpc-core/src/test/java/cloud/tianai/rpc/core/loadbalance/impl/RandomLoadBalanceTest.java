package cloud.tianai.rpc.core.loadbalance.impl;


import cloud.tianai.remoting.api.RemotingClient;

import java.util.ArrayList;
import java.util.List;

public class RandomLoadBalanceTest {


    public static void main(String[] args) {
        RandomLoadBalance randomLoadBalance = new RandomLoadBalance();

        List<RemotingClient> rpcClients = new ArrayList<>();
        RemotingClient r1 = new RemotingClientTestImpl();
        r1.setWeight(100);

        RemotingClient r2 = new RemotingClientTestImpl();
        r2.setWeight(200);

        RemotingClient r3 = new RemotingClientTestImpl();
        r3.setWeight(50);

        rpcClients.add(r1);
        rpcClients.add(r2);
        rpcClients.add(r3);
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < 100; i++) {
            RemotingClient remotingClient = randomLoadBalance.doSelect(rpcClients, null);
            if(100 == remotingClient.getWeight()) {
                a++;
            }else if(200 == remotingClient.getWeight()) {
                b++;
            }else {
                c++;
            }
        }
        System.out.println("a=" + a +",b=" + b +",c=" + c);
    }
}