package cloud.tianai.rpc.common;

import cloud.tianai.rpc.common.constant.CommonConstant;
import lombok.Data;

@Data
public class RpcConfiguration {

    private URL registryUrl;

    private String codec = "hessian2";

    private Integer timeout = 5000;

    private String protocol = "netty";

    private Integer workerThread = CommonConstant.DEFAULT_IO_THREADS;

    public <T> T getOrDefault(T val, T defVal) {
        if(val == null) {
            return defVal;
        }
        return val;
    }
}
