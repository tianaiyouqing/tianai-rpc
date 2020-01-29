package cloud.tianai.rpc.demo.rpc;

import cloud.tianai.rpc.common.util.id.IdUtils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class DemoImpl implements Demo, Serializable {

    @Override
    public String sayHello() {
        System.out.println("sayHello调用 -> 休眠一秒返回, threadId" + Thread.currentThread().getId() +", name " + Thread.currentThread().getName());
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello rpc";
    }

    @Override
    public DemoRes helloRpc() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DemoRes demoRes = new DemoRes();
        demoRes.setId(IdUtils.getNoRepetitionIdStr());
        demoRes.setData("hello im tianai-rpc");
        System.out.println("返回数据, 1 -> threadName:" + Thread.currentThread().getName());
        return demoRes;
    }
}
