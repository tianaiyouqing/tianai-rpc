package cloud.tianai.remoting.api;


public interface HighRpcInvocation extends RpcInvocation {

    /**
     * 添加invoke对象
     * @param interfaceClass
     * @param ref
     */
    void putInvokeObj(Class<?> interfaceClass, Object ref);

    /**
     * 添加处理器， 请求前，请求后，请求异常等等
     * @param postProcessor
     */
    void addPostProcessor(RpcInvocationPostProcessor postProcessor);
}
