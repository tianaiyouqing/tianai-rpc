package cloud.tianai.rpc.demo.rpc;


import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcServerImplTest2 {

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.codec("hessian2")
                    .timeout(5000)
                    .registry(new URL("zookeeper", "127.0.0.1", 2181))
                    .server("netty")
                    .port(20886)
                    .register(Demo.class, new DemoImpl())
                    .register(Demo2.class, new DemoImpl2())
                    .start();
            // 注册
            System.out.println("启动成功");
        }).start();

        TimeUnit.HOURS.sleep(1);
    }
}