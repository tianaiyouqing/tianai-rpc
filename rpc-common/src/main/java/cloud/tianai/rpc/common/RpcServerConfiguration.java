package cloud.tianai.rpc.common;


import cloud.tianai.rpc.common.util.IPUtils;
import lombok.Data;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:50
 * @Description: RPC Server 配置
 */
@Data
public class RpcServerConfiguration extends RpcConfiguration{

    /** Host 地址. */
    private String host = IPUtils.getHostIp();

    /** 端口. */
    private Integer port = 20881;

    /** boss线程. */
    private Integer bossThreads = 1;
}
