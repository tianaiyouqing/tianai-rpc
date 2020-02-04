package cloud.tianai.rpc.common;


import cloud.tianai.rpc.common.util.IPUtils;
import lombok.Data;

@Data
public class RpcServerConfiguration extends RpcConfiguration{


    private String host = IPUtils.getHostIp();

    private Integer port = 20881;

    private Integer bossThreads = 1;
}
