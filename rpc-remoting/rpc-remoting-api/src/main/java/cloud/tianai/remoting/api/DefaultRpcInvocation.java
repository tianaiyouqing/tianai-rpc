package cloud.tianai.remoting.api;

import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRpcInvocation extends AbstractHignRpcInvocation {


    @Override
    protected Object doInvoke(Method method, Object invokeObj, Request request) throws Exception{
        return method.invoke(invokeObj, request.getRequestParam());
    }
}
