package cloud.tianai.rpc.remoting.netty;

import cloud.tianai.rpc.remoting.api.RemotingDataProcessor;
import cloud.tianai.rpc.remoting.api.Request;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 21:45
 * @Description: Netty Handler
 */
@Slf4j
public class NettyClientHandler extends ChannelDuplexHandler {
    private RemotingDataProcessor remotingDataProcessor;
    private NettyChannelAdapter channelAdapter;

    public NettyClientHandler(RemotingDataProcessor remotingDataProcessor) {
        this.remotingDataProcessor = remotingDataProcessor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channelAdapter = new NettyChannelAdapter(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (remotingDataProcessor.support(msg)) {
            // 该解析器如果支持，则调用该解析器解析
            remotingDataProcessor.readMessage(channelAdapter, msg, ctx);
        } else {
            // 负责使用默认通道继续执行
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        msg = remotingDataProcessor.writeMessage(channelAdapter, msg, ctx);
        if (msg != null) {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            Request req = new Request();
            req.setHeartbeat(true);
            ctx.writeAndFlush(req);
            remotingDataProcessor.sendHeartbeat(channelAdapter, ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}



