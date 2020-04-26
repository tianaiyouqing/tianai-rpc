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
     * 默认远程的protocol
     */
    String DEFAULT_REMOTING_PROTOCOL = "netty";

    /**
     * 默认的工作线程数
     */
    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    /**
     * 默认重试次数3次.
     */
    int DEFAULT_REQUEST_RETRY = 3;

    /** 默认的 端口号. */
    int DEFAULT_SERVER_PORT = 20881;


    /**
     * RPC代理的PROTOCOL
     */
    String RPC_PROXY_PROTOCOL = "tianai-rpc";


    // ====================
    // 超时相关
    // ====================
    String TIMEOUT_KEY = "timeout";

    /**  默认超时. */
    int DEFAULT_TIMEOUT = 5000;
    /** 默认的请求超时. */
    int DEFAULT_REQUEST_TIMEOUT = 5000;


    // ====================
    // 权重相关
    // ====================
    /** 权重的key值. */
    String WEIGHT_KEY = "weight";
    /** 默认权重值. */
    Integer DEFAULT_WEIGHT = 100;


    // ====================
    // 序列化相关
    // ====================
    /** 编解码器的key. */
    String CODEC_KEY = "codec";
    /** 默认的编码解码器. */
    String DEFAULT_CODEC = "hessian2";


    // ====================
    // RPC远程连接配置 相关 key
    // ====================
    /** 工作线程 key. */
    String RPC_WORKER_THREADS_KEY = "workerThreads";
    /** boss线程 key. */
    String RPC_BOSS_THREAD_KEY = "bossThreads";
    /** connectTimeout key. */
    String RPC_CONNECT_TIMEOUT_KEY = "connectTimeout";
    /** idleTimeout key. */
    String RPC_IDLE_TIMEOUT_KEY = "idleTimeout";
    String RPC_SERVER_IDLE_TIMEOUT_KEY = "serverIdleTimeout";

    // RPC 远程连接配置相关默认值

    /** 默认的连接超时时间. */
    int DEFAULT_RPC_CONNECT_TIMEOUT = 5000;
    /** 默认 boss线程数. */
    int DEFAULT_RPC_BOSS_THREAD = 1;
    /** 默认的心跳超时时间. */
    int DEFAULT_RPC_IDLE_TIMEOUT = 600000;
    /** 默认server端心跳超时时间. */
    int DEFAULT_RPC_SERVER_IDLE_TIMEOUT = 600000 * 3;
}
