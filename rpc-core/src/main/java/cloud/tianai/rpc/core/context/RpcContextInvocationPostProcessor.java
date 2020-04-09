package cloud.tianai.rpc.core.context;

import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.remoting.api.Response;
import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.common.sort.Ordered;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2020/03/03 13:05
 * @Description: RpcContext 的数据封装
 */
public class RpcContextInvocationPostProcessor implements RpcInvocationPostProcessor, Ordered {

    @Override
    public Response beforeInvoke(Method method, Request request, Object invokeObj) {
        RpcContext rpcContext = RpcContext.getRpcContext();
        Map<String, Object> headers = request.getHeaders();
        // 设置header信息到 attachments
        rpcContext.setAttachments(headers);
        // 设置request信息到 RpcContext
        rpcContext.setRequest(request);
        return null;
    }


    @Override
    public Response invokeFinished(Request request, Response response, Method method, Object invokeObj) {
        RpcContext.removeContext();
        return response;
    }

    @Override
    public int getOrder() {
        // 最先执行
        return HIGHEST_PRECEDENCE + 1;
    }
}
