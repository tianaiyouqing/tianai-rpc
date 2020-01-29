package cloud.tianai.rpc.demo.rpc;

import cloud.tianai.rpc.common.util.id.IdUtils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class DemoImpl2 implements Demo2, Serializable {

    @Override
    public String sayHello2() {
        System.out.println("sayHello调用 -> 休眠一秒返回, threadId" + Thread.currentThread().getId() +", name " + Thread.currentThread().getName());
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello rpc 2";
    }

    @Override
    public DemoRes helloRpc2() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DemoRes demoRes = new DemoRes();
        demoRes.setId(IdUtils.getNoRepetitionIdStr());
        demoRes.setData("hello im tianai-rpc [2]");
        System.out.println("返回数据, threadName:" + Thread.currentThread().getName());
        return demoRes;
    }
}
