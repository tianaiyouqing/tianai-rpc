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
        if (msg instanceof Request) {
            // 解析Request
            Response response = rpcInvocation.invoke((Request) msg);
            channel.write(response);
        } else {
            // 解析Response
            DefaultFuture.received(channel, (Response) msg, true);
        }
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
