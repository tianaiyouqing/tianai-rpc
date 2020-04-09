package cloud.tianai.rpc.remoting.api;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 16:08
 * @Description: Response
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class Response extends Header implements Serializable {
    /**
     * ok.
     */
    public static final byte OK = 20;

    /**
     * client side timeout.
     */
    public static final byte CLIENT_TIMEOUT = 30;

    /**
     * server side timeout.
     */
    public static final byte SERVER_TIMEOUT = 31;

    /**
     * channel inactive, directly return the unfinished requests.
     */
    public static final byte CHANNEL_INACTIVE = 35;

    /**
     * request format error.
     */
    public static final byte BAD_REQUEST = 40;

    /**
     * response format error.
     */
    public static final byte BAD_RESPONSE = 50;

    /**
     * service not found.
     */
    public static final byte SERVICE_NOT_FOUND = 60;

    /**
     * service error.
     */
    public static final byte SERVICE_ERROR = 70;

    /**
     * internal server error.
     */
    public static final byte SERVER_ERROR = 80;

    /**
     * internal server error.
     */
    public static final byte CLIENT_ERROR = 90;

    /**
     * server side threadpool exhausted and quick return.
     */
    public static final byte SERVER_THREADPOOL_EXHAUSTED_ERROR = 100;


    /** ID. */
    private Long id;
    /** 版本. */
    private String version;
    /** 心跳. */
    private boolean heartbeat;

    /** 状态. */
    private byte status = OK;

    private Object result;

    private String errorMessage;

    public Response() {
    }

    public Response(Long id) {
        this.id = id;
    }

    public Response(Long id, String version) {
        this.id = id;
        this.version = version;
    }

}
