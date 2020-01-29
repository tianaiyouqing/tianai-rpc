package cloud.tianai.remoting.api;

import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRpcInvocation implements RpcInvocation {
    private  Map<Class<?>, Object> objectMap = new ConcurrentHashMap<>(256);

    public void put(Class<?> interfaceClass,  Object ref) {
        objectMap.remove(interfaceClass);
        objectMap.put(interfaceClass, ref);
    }

    public Map<Class<?>, Object> getObjectMap() {
        return objectMap;
    }

    @Override
    public Object invoke(Request request) {
        Class<?> interfaceType = request.getInterfaceType();
        Object invokeObj = objectMap.get(interfaceType);
        String methodName = request.getMethodName();
        if(Objects.isNull(invokeObj)) {
            throw new RpcException("rpc调用异常, 未找到对应的实例[" + interfaceType +"]");
        }
        Object[] requestParam = request.getRequestParam();
        Class<?>[] requestParamType = ClassUtils.getType(requestParam);
        try {
            Method method = invokeObj.getClass().getMethod(methodName, requestParamType);
            Object res = method.invoke(invokeObj, requestParam);
            return res;
        } catch (Exception e) {
            // 直接抛出去
            throw new RpcException(e);
        }
    }
}
