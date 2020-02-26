package cloud.tianai.remoting.api.util;

import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.Response;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/25 22:40
 * @Description: Response 工具类
 */
public class ResponseUtils {

    public static Response warpResponse(Throwable e, Request request) {
        Throwable targetException;
        if (e instanceof InvocationTargetException) {
            targetException = ((InvocationTargetException) e).getTargetException();
        }else {
            targetException = e;
        }

        long id = request.getId();
        String version = request.getVersion();
        boolean heartbeat = request.isHeartbeat();

        Response response = new Response(id, version);
        response.setHeartbeat(heartbeat);
        response.setStatus(Response.SERVER_ERROR);
        response.setErrorMessage(targetException.getLocalizedMessage());
        response.setResult(targetException);
        return response;
    }

    public static Response warpResponse(Object result, Request request) {
        Response response;
        long id = request.getId();
        String version = request.getVersion();
        boolean heartbeat = request.isHeartbeat();
        if (result instanceof Response) {
            response = (Response) result;
            response.setId(id);
            response.setVersion(version);
        } else {
            response = new Response(id, version);
            response.setHeartbeat(heartbeat);
            response.setResult(result);
        }
        return response;
    }
}
