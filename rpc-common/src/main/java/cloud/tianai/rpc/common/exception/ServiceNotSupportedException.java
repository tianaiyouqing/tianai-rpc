package cloud.tianai.rpc.common.exception;


/**
 * @Author: 天爱有情
 * @date 2021/1/15 16:24
 * @Description 服务不支持异常
 */
public class ServiceNotSupportedException extends RpcException {

    public ServiceNotSupportedException() {
    }

    public ServiceNotSupportedException(String message) {
        super(message);
    }

    public ServiceNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotSupportedException(Throwable cause) {
        super(cause);
    }

    public ServiceNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
