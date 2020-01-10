package cloud.tianai.remoting.api;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import lombok.Data;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 12:35
 * @Description: 远程服务相关配置
 */
@Data
public class RemotingConfiguration {
    /** ip. */
    private String host;
    /** 端口. */
    private Integer port;
    /** 工作线程数. */
    private Integer workerThreads;
    /** 编码器. */
    private RemotingDataEncoder encoder;
    /** 解码器. */
    private RemotingDataDecoder decoder;
    /** 远程数据解析. */
    private RemotingDataProcessor remotingDataProcessor;
    /** 链接超时. */
    private Integer connectTimeout;
}
