package cloud.tianai.rpc.registry.zookeeper;


import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.registory.api.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZookeeperRegistryTest {

    public static void main(String[] args) {

        URL url = new URL("zookeeper", "127.0.0.1", 2181);
        Registry registry = new ZookeeperRegistry();



        URL url1 = new URL("defaultrpc", "127.0.0.1", 21881, Demo1.class.toString());
        registry.subscribe(url1, (List<URL> urls) -> {
            System.out.println("监听订阅: " + urls);
        });

        registry.register(url1);

        URL url2 = new URL("defaultrpc", "127.0.0.1", 21882, Demo1.class.toString());

        registry.register(url2);


        Result<List<URL>> listResult = registry.lookup(url1);
        System.out.println(listResult);
    }

}