package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.util.id.IdUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class AbstractRemotingEndpoint implements RemotingEndpoint {
    /**
     * 为TRUE表示已启动.
     */
    private AtomicBoolean start = new AtomicBoolean(false);

    private String id;

    /**
     * 默认重量都是100
     */
    private int weight = 100;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public RemotingChannelHolder start(RemotingConfiguration config) throws RpcRemotingException {
        if (!start.compareAndSet(false, true)) {
            throw new RpcRemotingException("无需重复启动");
        }
        this.id = IdUtils.getNoRepetitionIdStr();
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

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int getWeight() {
        return weight;
    }


    /**
     * 子类实现， 具体的start方法
     * @param config 启动配置
     * @return RemotingChannelHolder
     * @throws RpcRemotingException 启动失败抛出异常
     */
    protected abstract RemotingChannelHolder doStart(RemotingConfiguration config) throws RpcRemotingException;

    /**
     * 停止服务
     * @throws RpcRemotingException 可能会抛出异常
     */
    protected abstract void doStop() throws RpcRemotingException;
}
