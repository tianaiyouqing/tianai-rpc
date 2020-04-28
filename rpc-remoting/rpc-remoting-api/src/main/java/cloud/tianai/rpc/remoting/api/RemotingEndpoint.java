package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.common.Node;
import cloud.tianai.rpc.common.ParametersWrapper;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;

import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/12 22:09
 * @Description: 远程 端点相关信息
 */
public interface RemotingEndpoint extends Node {
    /**
     * 启动远程
     * @param config 配置消息
     * @return channel持有者
     * @throws RpcRemotingException 启动失败会抛异常
     */
    RemotingChannelHolder start(URL config, RemotingDataProcessor remotingDataProcessor, Map<String, String> parameters) throws RpcRemotingException;

    /**
     * 是否打开状态
     * @return
     */
    boolean isOpen();

    /**
     * 获取唯一ID
     * @return
     */
    String getId();
    /**
     * 获取管道
     * @return
     */
    RemotingChannelHolder getChannel();

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

    /**
     * 设置权重
     * @param weight 权重，默认100
     */
    void setWeight(int weight);

    /**
     * 获取Weight
     * @return 对应的权重的值，越小越优先
     */
    int getWeight();

    /**
     * 获取参数信息
     * @return
     */
    ParametersWrapper getParameters();
}
