package cloud.tianai.rpc.common;


import lombok.Data;

@Data
public class RpcClientConfiguration extends RpcConfiguration{


    private String loadBalance;

    private Integer requestTimeout = 3000;

}
