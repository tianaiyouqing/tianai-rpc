package cloud.tianai.rpc.remoting.api;


/**
 * @Author: 天爱有情
 * @Date: 2020/04/12 22:25
 * @Description: RPC 执行器, 一般提供者用于调用接口对应的实现
 */
public interface RpcInvocation {

    /**
     * 执行 invoke
     * @param request 请求对象
     * @return Response 返回Response对象
     */
    Response invoke(Request request);
}
