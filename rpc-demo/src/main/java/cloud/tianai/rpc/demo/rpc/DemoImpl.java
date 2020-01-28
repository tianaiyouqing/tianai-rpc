package cloud.tianai.rpc.demo.rpc;

import java.io.Serializable;

public class DemoImpl implements Demo, Serializable {

    @Override
    public String sayHello() {
        System.out.println("sayHello调用");
        return "hello rpc";
    }
}
