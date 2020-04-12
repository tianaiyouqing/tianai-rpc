package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 15:29
 * @Description: 抽象的远程server
 */
@Slf4j
public abstract class AbstractRemotingServer extends AbstractRemotingEndpoint implements RemotingServer {
    @Override
    public RemotingChannelHolder start(RemotingConfiguration config) throws RpcRemotingException {
        if (config instanceof RemotingServerConfiguration) {
            return super.start(config);
        } else {
            throw new RpcRemotingException("配置 server端需要传入 [RemotingServerConfiguration] 而不是 [RemotingConfiguration]");
        }
    }

    @Override
    protected RemotingChannelHolder doStart(RemotingConfiguration config) throws RpcRemotingException {
        return doStart((RemotingServerConfiguration) config);
    }

    /**
     * 具体的 server端启动方法，子类实现
     * @param config 配置消息
     * @return RemotingChannelHolder
     * @throws RpcRemotingException 启动失败抛出异常
     */
    protected abstract RemotingChannelHolder doStart(RemotingServerConfiguration config) throws RpcRemotingException;
}
