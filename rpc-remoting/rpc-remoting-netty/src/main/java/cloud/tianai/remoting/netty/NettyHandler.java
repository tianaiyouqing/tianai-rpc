package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.RemotingDataProcessor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 21:45
 * @Description: Netty Handler
 */
@Slf4j
public class NettyHandler extends ChannelDuplexHandler {
    private RemotingDataProcessor remotingDataProcessor;

    public NettyHandler(RemotingDataProcessor remotingDataProcessor) {
        this.remotingDataProcessor = remotingDataProcessor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (remotingDataProcessor.support(msg)) {
            // 该解析器如果支持，则调用该解析器解析
            NettyChannelAdapter channelAdapter = new NettyChannelAdapter(ctx.channel());
            remotingDataProcessor.readMessage(channelAdapter, msg, ctx);
        } else {
            // 负责使用默认通道继续执行
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        NettyChannelAdapter channelAdapter = new NettyChannelAdapter(ctx.channel());
        remotingDataProcessor.writeMessage(channelAdapter, msg, ctx);
        super.write(ctx, msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException) {
            log.warn("netty IO异常: e=" + cause.getMessage());
        }else {
            cause.printStackTrace();
        }
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }
}
