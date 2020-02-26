package cloud.tianai.rpc.common.configuration;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import lombok.Data;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:49
 * @Description: RPC相关配置
 */
@Data
public class RpcConfiguration {

   /** 服务注册地址. */
    private URL registryUrl;
    /** 序列化. */
    private String codec = "hessian2";

    /** 超时. */
    private Integer timeout = 5000;

    /** 远程类型. */
    private String protocol = "netty";

    /** 工作线程. */
    private Integer workerThread = CommonConstant.DEFAULT_IO_THREADS;

    public <T> T getOrDefault(T val, T defVal) {
        if (val == null) {
            return defVal;
        }
        return val;
    }
}
