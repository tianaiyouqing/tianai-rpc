package cloud.tianai.rpc.core.client;

import cloud.tianai.remoting.api.Request;

public class RpcClientLoadBalancedProxy implements RpcClient {

    //TODO: add implementation details & states

    @Override
    public Object request(Request request) {
        return loadBalance(request).request(request);
    }

    private RpcClient loadBalance(Request request) {
        throw new UnsupportedOperationException();
    }
}
