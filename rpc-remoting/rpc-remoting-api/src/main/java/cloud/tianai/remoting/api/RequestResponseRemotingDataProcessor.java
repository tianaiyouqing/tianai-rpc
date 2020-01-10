package cloud.tianai.remoting.api;

public class RequestResponseRemotingDataProcessor implements RemotingDataProcessor {

    private RpcInvocation rpcInvocation;

    public RequestResponseRemotingDataProcessor(RpcInvocation rpcInvocation) {
        this.rpcInvocation = rpcInvocation;
    }

    @Override
    public void readMessage(Channel channel, Object msg, Object extend) {
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
        try {
            Object result = rpcInvocation.invoke(copyReq);
            return warpResponse(result, request);
        } catch (Throwable e) {
            // 异常
            return warpResponse(e, request);
        }
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
    public boolean support(Object msg) {
        // 只支持 Request 和 Response
        return msg instanceof Request || msg instanceof Response;
    }
}
