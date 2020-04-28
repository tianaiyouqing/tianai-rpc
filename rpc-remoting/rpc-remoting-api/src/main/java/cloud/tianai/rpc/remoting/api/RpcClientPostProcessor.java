package cloud.tianai.rpc.remoting.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/26 21:03
 * @Description: RPC 客户端解析器
 */
public interface RpcClientPostProcessor {

    /**
     * 请求之前
     * @param request 请求体
     */
    void beforeRequest(Request request, RemotingClient remotingClient);

    /**
     * 请求完成后
     * @param request 请求体
     * @param response 返回数据
     */
    void requestFinished(Request request, Response response, RemotingClient remotingClient);
}
