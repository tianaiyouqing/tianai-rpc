package cloud.tianai.rpc.core.client;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.exception.RpcChannelClosedException;
import cloud.tianai.rpc.common.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RpcClientDefaultImpl implements RpcClient {

    private final RemotingClient remotingClient;

    private final int timeout;

    public RpcClientDefaultImpl(RemotingClient remotingClient, int timeout) {

        assert remotingClient != null;
        assert timeout > 0;

        this.remotingClient = remotingClient;
        this.timeout = timeout;
    }

    @Override
    public Object request(Request request) {

        CompletableFuture<Object> future = remotingClient.getChannel().request(request, timeout);

        Object resObj;
        try {
            resObj = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            if (ex.getCause() instanceof RpcChannelClosedException) {
                remotingClient.doConnect();
            }
            throw new RpcException(ex);
        }
        return resObj;
    }
}
