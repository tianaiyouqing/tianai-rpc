package cloud.tianai.remoting.api;

import cloud.tianai.remoting.api.exception.RpcRemotingException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class AbstractRemotingEndpoint implements RemotingEndpoint {
    /**
     * 为TRUE表示已启动.
     */
    private AtomicBoolean start = new AtomicBoolean(false);

    @Override
    public RemotingChannelHolder start(RemotingConfiguration config) throws RpcRemotingException {
        if (!start.compareAndSet(false, true)) {
            throw new RpcRemotingException("无需重复启动");
        }
        try {
            RemotingChannelHolder channelHolder = doStart(config);
            return channelHolder;
        } catch (RpcRemotingException e) {
            start.set(false);
            throw e;
        } catch (Exception e) {
            start.set(false);
            throw new RpcRemotingException(e);
        }
    }


    @Override
    public void stop() {
        if (start.compareAndSet(true, false)) {
            try {
                doStop();
            } catch (Throwable e) {
                log.warn("停止server失败 , 类型[{}], e: [{}]", getRemotingType(), e);
            }
        }
    }

    @Override
    public boolean isStart() {
        return start.get();
    }

    protected abstract RemotingChannelHolder doStart(RemotingConfiguration config) throws RpcRemotingException;

    protected abstract void doStop() throws RpcRemotingException;
}
