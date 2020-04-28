package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static cloud.tianai.rpc.common.constant.CommonConstant.*;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 15:29
 * @Description: 抽象的远程server
 */
@Slf4j
@Getter
public abstract class AbstractRemotingServer extends AbstractRemotingEndpoint implements RemotingServer {

    /** boss线程数. */
    private int bossThreads;
    /** server 心跳超时. */
    private int serverIdleTimeout;
    @Override
    protected void prepareStart() {
        this.bossThreads = getParameters().getParameter(RPC_BOSS_THREAD_KEY, DEFAULT_RPC_BOSS_THREAD);
        this.serverIdleTimeout = getParameters().getParameter(RPC_SERVER_IDLE_TIMEOUT_KEY, getIdleTimeout() * 3);
    }
}
