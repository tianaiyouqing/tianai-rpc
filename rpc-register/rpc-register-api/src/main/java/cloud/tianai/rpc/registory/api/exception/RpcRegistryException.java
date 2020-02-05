package cloud.tianai.rpc.registory.api.exception;

import cloud.tianai.rpc.common.exception.RpcException;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/03 12:09
 * @Description: RPC REGISTRY 异常
 */
public class RpcRegistryException extends RpcException {

    public RpcRegistryException() {
    }

    public RpcRegistryException(String message) {
        super(message);
    }

    public RpcRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcRegistryException(Throwable cause) {
        super(cause);
    }

    public RpcRegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
