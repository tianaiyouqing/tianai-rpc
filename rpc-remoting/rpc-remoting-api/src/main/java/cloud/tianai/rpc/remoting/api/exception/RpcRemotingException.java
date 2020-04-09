package cloud.tianai.rpc.remoting.api.exception;

import cloud.tianai.rpc.common.exception.RpcException;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 11:51
 * @Description: RPC远程相关异常
 */
public class RpcRemotingException extends RpcException {

    public RpcRemotingException() {
    }

    public RpcRemotingException(String message) {
        super(message);
    }

    public RpcRemotingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcRemotingException(Throwable cause) {
        super(cause);
    }

    public RpcRemotingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
