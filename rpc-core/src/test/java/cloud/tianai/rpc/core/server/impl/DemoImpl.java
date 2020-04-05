package cloud.tianai.rpc.core.server.impl;

import java.io.Serializable;
import java.util.Properties;

public class DemoImpl implements Demo, Serializable {

    @Override
    public String sayHello() {
        return "hello rpc";
    }

}
