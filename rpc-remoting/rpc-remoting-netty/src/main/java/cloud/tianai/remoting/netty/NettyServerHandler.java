package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.RemotingDataProcessor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 21:45
 * @Description: Netty Handler
 */
@Slf4j
public class NettyServerHandler extends ChannelDuplexHandler {
    private RemotingDataProcessor remotingDataProcessor;
    ExecutorService executorService;

    public NettyServerHandler(ExecutorService executorService,
                              RemotingDataProcessor remotingDataProcessor) {
        this.executorService = executorService;
        this.remotingDataProcessor = remotingDataProcessor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (executorService != null) {
            executorService.execute(new NettyRunnable(ctx, msg, remotingDataProcessor));
        } else {
            // 当前线程执行
            new NettyRunnable(ctx, msg, remotingDataProcessor).run();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        NettyChannelAdapter channelAdapter = new NettyChannelAdapter(ctx.channel());
        msg = remotingDataProcessor.writeMessage(channelAdapter, msg, ctx);
        if(msg != null) {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 回送异常信息

        ctx.close();
        super.exceptionCaught(ctx, cause);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            // 心跳如果无响应，直接关闭通道
            ctx.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    public static class NettyRunnable implements Runnable {
        private ChannelHandlerContext ctx;
        private Object msg;
        private RemotingDataProcessor processor;

        public NettyRunnable(ChannelHandlerContext ctx, Object msg, RemotingDataProcessor processor) {
            this.ctx = ctx;
            this.msg = msg;
            this.processor = processor;
        }

        @Override
        public void run() {
            if (processor.support(msg)) {
                // 该解析器如果支持，则调用该解析器解析
                NettyChannelAdapter channelAdapter = new NettyChannelAdapter(ctx.channel());
                processor.readMessage(channelAdapter, msg, ctx);
            } else {
                // 负责使用默认通道继续执行
                try {
                    ctx.fireChannelRead(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}



