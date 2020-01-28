package cloud.tianai.rpc.core.loadbalance.impl;


import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.client.RpcClient;
import cloud.tianai.rpc.core.client.impl.RpcClientImpl;

import java.util.ArrayList;
import java.util.List;

public class RoundRobinLoadBalanceTest {

    public static void main(String[] args) {
        RoundRobinLoadBalance roundRobinLoadBalance = new RoundRobinLoadBalance();
        List<RpcClient> rpcClients = new ArrayList<>(10);
        rpcClients.add(new RpcClientImpl("1"));
        rpcClients.add(new RpcClientImpl("2"));
        rpcClients.add(new RpcClientImpl("3"));
        rpcClients.add(new RpcClientImpl("4"));
        rpcClients.add(new RpcClientImpl("5"));
        rpcClients.add(new RpcClientImpl("6"));
        rpcClients.add(new RpcClientImpl("7"));
        rpcClients.add(new RpcClientImpl("8"));
        rpcClients.add(new RpcClientImpl("9"));
        rpcClients.add(new RpcClientImpl("10"));

        URL url = new URL("netty", "127.0.0.1", 0, "cloud.tianai.rpc.core.server.RpcServer");

        Request request = new Request();
        request.setInterfaceType(String.class);
        request.setMethodName("toString");

        for (int i = 0; i < 20; i++) {
            RpcClient select = roundRobinLoadBalance.select(rpcClients, url, request);
            System.out.println(select.getId());
        }
    }
}