package cloud.tianai.rpc.demo.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class DemoResult implements Serializable {

    private String str;

    private Integer id;

    DemoRequest request;
}
