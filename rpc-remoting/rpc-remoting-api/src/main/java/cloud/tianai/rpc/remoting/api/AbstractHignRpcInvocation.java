package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.remoting.api.util.ResponseUtils;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractHignRpcInvocation implements HighRpcInvocation {

    /**
     * 心跳直接返回的数据.
     */
    public static final String HEARTBEAT_RESULT = "heartbeat success";

    private Map<Class<?>, Object> objectMap = new ConcurrentHashMap<>(256);

    private List<RpcInvocationPostProcessor> postProcessors = new ArrayList<>(8);

    @Override
    public void putInvokeObj(Class<?> interfaceClass, Object ref) {
        objectMap.remove(interfaceClass);
        objectMap.put(interfaceClass, ref);
    }

    @Override
    public void addPostProcessor(RpcInvocationPostProcessor postProcessor) {
        postProcessors.remove(postProcessor);
        postProcessors.add(postProcessor);
    }

    @Override
    public Response invoke(Request request) {
        if (Objects.isNull(request)) {
            // 如果是空，直接抛异常
            throw new RpcException("rpc调用异常, 参数[request] 不能为空");
        }
        Object invokeResult;
        Response response;
        // 1. 判断如果是心跳请求，直接返回心跳响应
        if (request.isHeartbeat()) {
            invokeResult = HEARTBEAT_RESULT;
            response = ResponseUtils.warpResponse(invokeResult, request);
        } else {
            // 2. 如果不是心跳请求，直接从里面找到invokes中进行invoke请求
            Object invokeObj = getInvokeObj(request);
            String methodName = request.getMethodName();
            Object[] requestParam = request.getRequestParam();
            Class<?>[] requestParamType = ClassUtils.getType(requestParam);
            Method method = null;
            try {
                // 执行请求
                // 执行前
                method = invokeObj.getClass().getMethod(methodName, requestParamType);
                response = beforeInvoke(method, request, invokeObj);
                if (response == null) {
                    // 执行默认
                    invokeResult = doInvoke(method, invokeObj, request);
                    response = ResponseUtils.warpResponse(invokeResult, request);
                    // 执行成功
                    response = invokeFinished(request, response, method, invokeObj);
                }
            } catch (Throwable e) {
                // 打印堆栈信息
                e.printStackTrace();
                // 异常
                response = ResponseUtils.warpResponse(e, request);
                // 执行异常
                response = invokeError(request, response, method, invokeObj, e);
            }
        }

        // 返回response消息
        return response;
    }

    protected Object getInvokeObj(Request request) {
        Class<?> interfaceType = request.getInterfaceType();
        Object invokeObj = objectMap.get(interfaceType);
        if (Objects.isNull(invokeObj)) {
            throw new RpcException("rpc调用异常, 未找到对应的实例[" + interfaceType + "]");
        }
        return invokeObj;
    }


    protected Response invokeError(Request request, Response response, Method method, Object invokeObj, Throwable e) {
        for (RpcInvocationPostProcessor postProcessor : postProcessors) {
            response = postProcessor.invokeError(request, response, method, invokeObj, e);
        }
        return response;
    }

    protected Response invokeFinished(Request request, Response response, Method method, Object invokeObj) {
        for (RpcInvocationPostProcessor postProcessor : postProcessors) {
            response = postProcessor.invokeFinished(request, response, method, invokeObj);
        }
        return response;
    }

    protected Response beforeInvoke(Method method, Request request, Object invokeObj) {
        Response response = null;
        for (RpcInvocationPostProcessor postProcessor : postProcessors) {
            response = postProcessor.beforeInvoke(method, request, invokeObj);
            if (response != null) {
                break;
            }
        }
        return response;
    }


    /**
     * 留给子类实现， 真正调用 invoke
     * @param method 方法
     * @param invokeObj invoke的对象
     * @param request 请求对象
     * @return 执行方法后返回的对象
     * @throws Exception 执行失败抛出异常
     */
    protected abstract Object doInvoke(Method method, Object invokeObj, Request request) throws Exception;
}
