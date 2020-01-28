package cloud.tianai.rpc.demo.rpc;

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
}
