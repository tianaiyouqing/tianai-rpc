package cloud.tianai.rpc.common.constant;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/29 13:31
 * @Description: 一些公共的常量
 */
public interface CommonConstant {

    /**
     * 重试.
     */
    String RETRY = "retry";
    String BACKUP_KEY = "backup";

    /**
     * 默认的编码解码器
     */
    String DEFAULT_CODEC = "hessian2";

    /**
     * 默认远程的protocol
     */
    String DEFAULT_REMOTING_PROTOCOL = "netty";
    /**
     * 默认超时
     */
    Integer DEFAULT_TIMEOUT = 5000;
    /**
     * 默认的工作线程数
     */
    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    /**
     * 默认重试次数3次.
     */
    int DEFAULT_REQUEST_RETRY = 3;

    /**
     * 默认的请求超时
     */
    int DEFAULT_REQUEST_TIMEOUT = 5000;

    /**
     * RPC代理的PROTOCOL
     */
    String RPC_PROXY_PROTOCOL = "tianai-rpc";
}
