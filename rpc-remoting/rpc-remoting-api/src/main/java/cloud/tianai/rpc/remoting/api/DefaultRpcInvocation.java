package cloud.tianai.rpc.remoting.api;

import java.lang.reflect.Method;

public class DefaultRpcInvocation extends AbstractHignRpcInvocation {


    @Override
    protected Object doInvoke(Method method, Object invokeObj, Request request) throws Exception{
        return method.invoke(invokeObj, request.getRequestParam());
    }
}
