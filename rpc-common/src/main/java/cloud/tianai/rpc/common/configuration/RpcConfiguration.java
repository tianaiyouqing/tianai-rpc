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



    public <T> T getOrDefault(T val, T defVal) {
        if (val == null) {
            return defVal;
        }
        return val;
    }
}
