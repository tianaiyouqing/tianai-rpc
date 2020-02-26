package cloud.tianai.remoting.api;

import cloud.tianai.remoting.api.util.ResponseUtils;

public class SimpleHeartbeatRpcInvocation implements RpcInvocation {
    @Override
    public Response invoke(Request request) {
        if (request.isHeartbeat()) {
            ResponseUtils.warpResponse("heart success", request);
        }
        return null;
    }
}
