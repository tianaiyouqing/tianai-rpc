package cloud.tianai.rpc.demo.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class DemoRequest implements Serializable {

    private Double price;

}
