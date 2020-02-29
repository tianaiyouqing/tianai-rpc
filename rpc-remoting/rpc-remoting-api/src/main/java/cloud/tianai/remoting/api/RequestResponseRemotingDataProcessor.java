package cloud.tianai.remoting.api;

import cloud.tianai.remoting.api.exception.RpcRemotingException;
import cloud.tianai.remoting.api.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.util.Objects;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 18:03
 * @Description: Request And Response 数据解析器
 */
@Slf4j
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
    public Object writeMessage(Channel channel, Object msg, Object extend) {
        if (support(msg)) {
            return msg;
        }
        Response response = null;
        // 处理异常
        if (msg instanceof Throwable) {
             response =  processThrowable((Throwable)msg, extend);
        }
        return response;
    }

    private Response processThrowable(Throwable ex, Object data) {
        Response response = null;
        if (data instanceof Request) {
            response = ResponseUtils.warpResponse(ex, (Request) data);
        }else if (data instanceof Response){
            response = ResponseUtils.warpResponse(ex, (Response) data);
        }
        return response;
    }


    @Override
    public void sendHeartbeat(Channel channel, Object extend) {
        Request request = new Request();
        request.setHeartbeat(true);
        channel.write(request);
    }

    @Override
    public void sendError(Channel channel, Throwable ex, Object data) {
        Response response = null;
        if (data instanceof Request) {
            response = ResponseUtils.warpResponse(ex, (Request) data);
        }else if (data instanceof Response) {
            response = ResponseUtils.warpResponse(ex, (Response) data);
        }else {
            log.error("发送异常信息失败, 参数不支持， data={}, ex={}", data, ex);
        }
        // 打印一下异常信息
        ex.printStackTrace();
        if (response != null) {
            channel.write(response);
        }
    }

    @Override
    public boolean support(Object msg) {
        // 只支持 Request 和 Response
        return msg instanceof Request || msg instanceof Response;
    }
}
