package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;
import lombok.Data;

import java.util.concurrent.ExecutorService;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 12:35
 * @Description: 远程服务相关配置
 */
@Data
public class RemotingConfiguration {
    /** ip. */
    private String host = IPUtils.getHostIp();
    /** 端口. */
    private Integer port = 20880;
    /** 工作线程数. */
    private Integer workerThreads = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
    /** 编码解码器. */
    private RemotingDataCodec codec;
    /** 远程数据解析. */
    private RemotingDataProcessor remotingDataProcessor;
    /** 链接超时. */
    private Integer connectTimeout = 5000;
    /** 工作线程池. */
    private ExecutorService threadPool;;
    /** 心跳超时. */
    private Integer idleTimeout = 600000;
}
