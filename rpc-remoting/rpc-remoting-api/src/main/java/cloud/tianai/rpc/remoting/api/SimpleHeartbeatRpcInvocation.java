package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.remoting.api.util.ResponseUtils;

public class SimpleHeartbeatRpcInvocation implements RpcInvocation {
    @Override
    public Response invoke(Request request) {
        if (request.isHeartbeat()) {
            ResponseUtils.warpResponse("heart success", request);
        }
        return null;
    }
}
