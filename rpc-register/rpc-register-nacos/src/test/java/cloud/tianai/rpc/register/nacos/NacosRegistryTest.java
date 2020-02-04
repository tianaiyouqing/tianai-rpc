package cloud.tianai.rpc.register.nacos;


import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NacosRegistryTest {


    public static void main(String[] args) throws InterruptedException {
        NacosRegistry nacosRegistry = new NacosRegistry();
        nacosRegistry.start(new URL("nacos", "127.0.0.1", 8848));
        nacosRegistry.subscribe(new URL("tianai-rpc", "127.0.0.1", 0, "demo"), (urls) -> {
            System.out.println("订阅到消息:" + urls);
        });
        nacosRegistry.register(new URL("tianai-rpc","127.0.0.1", 20881, "demo"));
        System.out.println("regiser");

//        for (int i = 0; i < 100; i++) {
//            nacosRegistry.register(new URL("tianai-rpc","127.0.0.1", i, "demo"));
//            TimeUnit.SECONDS.sleep(1);
//        }

        URL url = new URL("tianai-rpc", "127.0.0.1", 0, "demo");
        Result<List<URL>> res = nacosRegistry.lookup(url);
        System.out.println(res);

        TimeUnit.SECONDS.sleep(100);
    }
}