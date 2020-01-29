package cloud.tianai.rpc.demo.rpc;

import lombok.Data;

import java.io.Serializable;

@Data
public class DemoRes implements Serializable {
    private String id;
    private Object data;

}
