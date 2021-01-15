package cloud.tianai.rpc.remoting.codec.api.exception;

import cloud.tianai.rpc.common.exception.ServiceNotSupportedException;

/**
 * @Author: 天爱有情
 * @date 2021/1/15 16:37
 * @Description 编码解码异常
 */
public class CodecException extends ServiceNotSupportedException {

    public CodecException() {
    }

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecException(Throwable cause) {
        super(cause);
    }

    public CodecException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
