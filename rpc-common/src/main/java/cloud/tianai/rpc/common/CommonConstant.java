package cloud.tianai.rpc.common;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/29 13:31
 * @Description:  一些公共的常量
 */
public interface CommonConstant {

    /** 重试. */
    String RETRY = "retry";

    String DEFAULT_CODEC = "hessian2";
    String DEFAULT_PROTOCOL = "netty";
    Integer DEFAULT_TIMEOUT = 5000;
    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);


}
