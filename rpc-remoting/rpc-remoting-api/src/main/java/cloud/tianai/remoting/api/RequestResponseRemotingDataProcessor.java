package cloud.tianai.remoting.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 18:03
 * @Description: Request And Response 数据解析器
 */
public class RequestResponseRemotingDataProcessor implements RemotingDataProcessor {

    private RpcInvocation rpcInvocation;

    public RequestResponseRemotingDataProcessor(RpcInvocation rpcInvocation) {
        this.rpcInvocation = rpcInvocation;
    }

    @Override
    public void readMessage(Channel channel, Object msg, Object extend) {
        // 这里改成异步执行试试
        if (msg instanceof Request) {
            // 解析Request
            Response response = processRequest((Request) msg);
            channel.write(response);
        } else {
            // 解析Response
            DefaultFuture.received(channel, (Response) msg, true);
        }
    }

    private Response processRequest(Request request) {
        Request copyReq = Request.copyRequest(request);
        Object result;
        if (request.isHeartbeat()) {
            // 如果是心跳请求，直接返回
            result = "heartbeat success";
        } else {
            try {
                result = rpcInvocation.invoke(copyReq);
            } catch (Throwable e) {
                // 打印堆栈信息
                e.printStackTrace();
                // 异常
                return warpResponse(e, request);
            }
        }
        return warpResponse(result, request);
    }

    private Response warpResponse(Throwable e, Request request) {
        long id = request.getId();
        String version = request.getVersion();
        boolean heartbeat = request.isHeartbeat();

        Response response = new Response(id, version);
        response.setHeartbeat(heartbeat);
        response.setStatus(Response.SERVER_ERROR);
        response.setErrorMessage(e.getMessage());
        return response;
    }

    private Response warpResponse(Object result, Request request) {
        Response response;
        long id = request.getId();
        String version = request.getVersion();
        boolean heartbeat = request.isHeartbeat();
        if (result instanceof Response) {
            response = (Response) result;
            response.setId(id);
            response.setVersion(version);
        } else {
            response = new Response(id, version);
            response.setHeartbeat(heartbeat);
            response.setResult(result);
        }
        return response;
    }

    @Override
    public Object writeMessage(Channel channel, Object msg, Object extend) {
        return null;
    }

    @Override
    public void sendHeartbeat(Channel channel, Object extend) {
        Request request = new Request();
        request.setHeartbeat(true);
        channel.write(request);
    }

    @Override
    public boolean support(Object msg) {
        // 只支持 Request 和 Response
        return msg instanceof Request || msg instanceof Response;
    }
}
