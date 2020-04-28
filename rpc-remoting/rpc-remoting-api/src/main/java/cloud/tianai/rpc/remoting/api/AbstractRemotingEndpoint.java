package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.constant.CommonConstant;
import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.common.ParametersWrapper;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.util.id.IdUtils;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static cloud.tianai.rpc.common.constant.CommonConstant.*;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/25 23:00
 * @Description: 抽象的 远程 端点相关信息
 */
@Slf4j
public abstract class AbstractRemotingEndpoint implements RemotingEndpoint {
    /**
     * 为TRUE表示已启动.
     */
    private AtomicBoolean start = new AtomicBoolean(false);

    /**
     * 唯一的ID标识
     */
    private String id;

    /**
     * 当前的URL
     */
    @Getter
    private URL url;

    /**
     * 远程数据解析器
     */
    @Getter
    private RemotingDataProcessor remotingDataProcessor;

    /**
     * 工作线程数
     */
    @Getter
    private int workerThreads;

    /**
     * 心跳超时
     */
    @Getter
    private int idleTimeout;

    /**
     * 编解码器
     */
    @Getter
    private RemotingDataCodec remotingDataCodec;

    /**
     * 默认权重都是100
     */
    private int weight = DEFAULT_WEIGHT;

    private Map<String, String> parameters;
    private ParametersWrapper parametersWrapper;

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public RemotingChannelHolder start(URL url,
                                       RemotingDataProcessor remotingDataProcessor,
                                       Map<String, String> parameters) throws RpcRemotingException {
        if (!start.compareAndSet(false, true)) {
            throw new RpcRemotingException("无需重复启动");
        }
        this.parameters = parameters;
        this.parametersWrapper = new ParametersWrapper(parameters);
        this.id = IdUtils.getNoRepetitionIdStr();
        this.url = url;
        this.remotingDataProcessor = remotingDataProcessor;
        this.workerThreads = parametersWrapper.getParameter(RPC_WORKER_THREADS_KEY, DEFAULT_IO_THREADS);
        this.idleTimeout = parametersWrapper.getParameter(RPC_IDLE_TIMEOUT_KEY, DEFAULT_RPC_IDLE_TIMEOUT);
        // 设置权重
        setWeight(parametersWrapper.getParameter(CommonConstant.WEIGHT_KEY, CommonConstant.DEFAULT_WEIGHT));
        String codecProtocol = parametersWrapper.getParameter(CODEC_KEY, DEFAULT_CODEC);
        // 加载 codec
        remotingDataCodec = ExtensionLoader.getExtensionLoader(RemotingDataCodec.class).getExtension(codecProtocol);
        prepareStart();

        try {
            RemotingChannelHolder channelHolder = doStart();
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
    public ParametersWrapper getParameters() {
        return parametersWrapper;
    }

    /**
     * 启动前 模板方法， 子类扩展
     */
    protected abstract void prepareStart();

    @Override
    public void destroy() {
        if (start.compareAndSet(true, false)) {
            try {
                doDestroy();
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
     *
     * @return RemotingChannelHolder
     * @throws RpcRemotingException 启动失败抛出异常
     */
    protected abstract RemotingChannelHolder doStart() throws RpcRemotingException;

    /**
     * 销毁服务
     *
     * @throws RpcRemotingException 可能会抛出异常
     */
    protected abstract void doDestroy() throws RpcRemotingException;
}
