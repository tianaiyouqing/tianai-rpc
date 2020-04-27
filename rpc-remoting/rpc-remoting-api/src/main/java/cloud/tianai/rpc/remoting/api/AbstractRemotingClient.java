package cloud.tianai.rpc.remoting.api;

import lombok.Data;
import lombok.Getter;

import static cloud.tianai.rpc.common.constant.CommonConstant.*;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 17:22
 * @Description: 抽象的client客户端
 */
@Getter
public abstract class AbstractRemotingClient extends AbstractRemotingEndpoint implements RemotingClient {

    /** 连接超时. */
    private int connectTimeout;

    @Override
    protected void prepareStart() {
        // 连接超时
        this.connectTimeout = getParametersWrapper().getParameter(RPC_CONNECT_TIMEOUT_KEY, DEFAULT_RPC_IDLE_TIMEOUT);
    }
}
