package cloud.tianai.rpc.common;


import lombok.Data;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:48
 * @Description: RPC客户端相关配置
 */
@Data
public class RpcClientConfiguration extends RpcConfiguration{

    private String loadBalance;

    private Integer requestTimeout = 3000;

    private Integer retry = 3;
}
