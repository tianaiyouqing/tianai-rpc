package cloud.tianai.rpc.core.context;

import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.remoting.api.Request;
import cloud.tianai.rpc.remoting.api.Response;
import cloud.tianai.rpc.common.sort.Ordered;
import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;

import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2020/03/03 13:05 
 * @Description: RpcContext 的数据封装
 */
public class RpcContextClientPostProcessor implements RpcClientPostProcessor, Ordered {

    @Override
    public void beforeRequest(Request request, RemotingClient remotingClient) {
        RpcContext rpcContext = RpcContext.getRpcContext();
        // 设置远程url
        rpcContext.setRemotingUrl(remotingClient.getUrl());
        Map<String, Object> attachments = rpcContext.getAttachments();
        // 设置附加信息到header
        attachments.forEach(request :: setHeader);
        // 设置RPCContext到Request中
        rpcContext.setRequest(request);
    }

    @Override
    public void requestFinished(Request request, Response response, RemotingClient remotingClient) {
        RpcContext.removeContext();
    }

    @Override
    public int getOrder() {
        // 最后才执行
        return LOWEST_PRECEDENCE;
    }
}
