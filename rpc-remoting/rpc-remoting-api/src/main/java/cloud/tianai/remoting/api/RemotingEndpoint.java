package cloud.tianai.remoting.api;

import cloud.tianai.remoting.api.exception.RpcRemotingException;

public interface RemotingEndpoint {
    /**
     * 启动远程
     * @param config 配置消息
     * @return channel持有者
     * @throws RpcRemotingException 启动失败会抛异常
     */
    RemotingChannelHolder start(RemotingConfiguration config) throws RpcRemotingException;


    /**
     * 是否打开状态
     * @return
     */
    boolean isOpen();

    /**
     * 是否处于活跃状态，并已经连接
     * @return
     */
    boolean isActive();
    /**
     * 获取唯一ID
     * @return
     */
    String getId();
    /**
     * 获取管道
     * @return
     */
    RemotingChannelHolder getchannel();

    /**
     * 停止
     */
    void stop();

    /**
     * 是否启动
     * @return boolean
     */
    boolean isStart();

    /**
     * 获取远程client类型
     * @return
     */
    String getRemotingType();
}
