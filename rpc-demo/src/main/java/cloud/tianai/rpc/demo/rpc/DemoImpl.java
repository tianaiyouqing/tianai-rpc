package cloud.tianai.rpc.demo.rpc;

import java.io.Serializable;

public class DemoImpl implements Demo, Serializable {

    @Override
    public String sayHello() {
        return "hello rpc";
    }
}
