package cloud.tianai.remoting.api;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 16:04
 * @Description: 请求对象
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class Request extends Header implements Serializable {

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    /** ID. */
    private long id;

    /** 版本. */
    private String version;

    /** 请求数据. */
    private Object[] requestParam;

    /** 方法名. */
    private String methodName;

    /** 接口类型. */
    private Class<?> interfaceType;

    /** 返回类型. */
    private Class<?> returnType;

    /** 是否是心跳请求. */
    private boolean heartbeat;

    public static Request copyRequest(Request request) {
        Request result = new Request();
        result.setId(request.getId());
        result.setVersion(request.getVersion());
        result.setRequestParam(request.getRequestParam());
        result.setMethodName(request.getMethodName());
        result.setInterfaceType(request.getInterfaceType());
        result.setReturnType(request.getReturnType());
        result.setHeartbeat(request.isHeartbeat());
        return result;
    }

    private static long newId() {
        // getAndIncrement() When it grows to MAX_VALUE, it will grow to MIN_VALUE, and the negative can be used as ID
        return INVOKE_ID.getAndIncrement();
    }

    public Request() {
        id = newId();
    }
}
