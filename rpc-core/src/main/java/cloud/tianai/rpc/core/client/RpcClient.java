package cloud.tianai.rpc.core.client;

import cloud.tianai.remoting.api.Request;

public interface RpcClient {

    Object request(Request request);
}
