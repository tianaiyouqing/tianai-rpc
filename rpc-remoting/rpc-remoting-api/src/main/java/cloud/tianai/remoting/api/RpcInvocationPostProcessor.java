package cloud.tianai.remoting.api;

import java.lang.reflect.Method;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/26 21:04
 * @Description: RPC执行后处理器
 */
public interface RpcInvocationPostProcessor {

    /**
     * 执行前
     * @param method 要执行的方法
     * @param request 请求体
     * @param invokeObj ref对象
     * @return 如果范湖IBU为空，则不往下处理, 也就是说，不为空就视为被拦截，返回的数据之间通过rpc框架写出
     */
    default Response beforeInvoke(Method method, Request request, Object invokeObj) {
        return null;
    }

    /**
     * 执行完成
     * @param request request
     * @param response response
     * @param method 方法
     * @param invokeObj ref
     * @return Response，返回的数据最终会通过rpc框架写出
     */
    default Response invokeFinished(Request request, Response response, Method method, Object invokeObj) {
        return response;
    }

    /**
     * 执行异常
     * @param request request
     * @param response response
     * @param method 方法
     * @param invokeObj 执行对象
     * @param ex 异常
     * @return 返回的数据最终会通过rpc框架写出
     */
    default Response invokeError(Request request, Response response, Method method, Object invokeObj, Throwable ex) {
        return response;
    }
}
