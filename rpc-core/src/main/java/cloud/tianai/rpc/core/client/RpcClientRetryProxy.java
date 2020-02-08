package cloud.tianai.rpc.core.client;

import cloud.tianai.remoting.api.Request;
import cloud.tianai.rpc.common.exception.RpcException;

public class RpcClientRetryProxy implements RpcClient {

    private final int retryTimes;

    private final RpcClient delegate;

    public RpcClientRetryProxy(RpcClient delegate, int retryTimes) {

        assert delegate != null;
        assert retryTimes > 0;

        this.delegate = delegate;
        this.retryTimes = retryTimes;
    }

    @Override
    public Object request(Request request) {

        for (int tryCount = 0; tryCount < retryTimes; tryCount++) {
            try {
                return delegate.request(request);
            } catch (RpcException ex) {
                //TODO: log exception here
            }
        }
        throw new RpcException("达到最大重试次数");
    }
}
