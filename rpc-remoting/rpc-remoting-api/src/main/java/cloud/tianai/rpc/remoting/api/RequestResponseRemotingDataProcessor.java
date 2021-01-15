package cloud.tianai.rpc.remoting.api;

import cloud.tianai.rpc.common.exception.ServiceNotSupportedException;
import cloud.tianai.rpc.remoting.api.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 18:03
 * @Description: Request And Response 数据解析器
 */
@Slf4j
public class RequestResponseRemotingDataProcessor implements RemotingDataProcessor {
    private RpcInvocation rpcInvocation;
    public static final Class<?>[] SUPPORT_PARAMS_CLASS = new Class[]{Request.class, Response.class};

    public RequestResponseRemotingDataProcessor(RpcInvocation rpcInvocation) {
        this.rpcInvocation = rpcInvocation;
    }

    @Override
    public void readMessage(Channel channel, Object msg, Object extend) {
        ChannelHolder.bind(channel);
        if (msg instanceof Request) {
            // 解析Request
            Response response = rpcInvocation.invoke((Request) msg);
            channel.write(response);
            // 发送完数据后处理
            afterReadMessage(response, channel, msg, extend);
        } else {
            // 解析Response
            DefaultFuture.received(channel, (Response) msg, true);
        }
        ChannelHolder.unBind();
    }

    private void afterReadMessage(Response response, Channel channel, Object msg, Object extend) {
        if (Response.SERVICE_NOT_SUPPORTED == response.getStatus()) {
            // 状态码为无法提供服务时 ， 关闭channel
            channel.close();
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
            response = processThrowable((Throwable) msg, extend);
        }
        if (response == null) {
            // 如果response为空，直接把管道关闭
            channel.close();
        }
        return response;
    }

    private Response processThrowable(Throwable ex, Object data) {
        Response response = null;
        if (data instanceof Request) {
            response = ResponseUtils.warpResponse(ex, (Request) data);
        } else if (data instanceof Response) {
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
        } else if (data instanceof Response) {
            response = ResponseUtils.warpResponse(ex, (Response) data);
        } else {
            log.error("发送异常信息失败, 参数不支持， data={}, ex={}", data, ex);
        }
        // 打印一下异常信息
        ex.printStackTrace();
        if (response != null) {
            channel.write(response);
        }
        if (ex instanceof ServiceNotSupportedException) {
            // 关闭通道
            channel.close();
        }
        ChannelHolder.unBind();
    }

    @Override
    public Class<?>[] getSupportParams() {
        return SUPPORT_PARAMS_CLASS;
    }
}
