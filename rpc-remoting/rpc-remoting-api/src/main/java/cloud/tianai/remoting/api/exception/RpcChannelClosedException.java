package cloud.tianai.remoting.api.exception;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/29 21:53 
 * @Description: 管道关闭异常
 */
public class RpcChannelClosedException extends RpcRemotingException {

    public RpcChannelClosedException() {
    }

    public RpcChannelClosedException(String message) {
        super(message);
    }

    public RpcChannelClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcChannelClosedException(Throwable cause) {
        super(cause);
    }

    public RpcChannelClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
