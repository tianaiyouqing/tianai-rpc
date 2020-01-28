package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.RemotingDataProcessor;
import cloud.tianai.rpc.common.threadpool.NamedThreadFactory;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 21:45
 * @Description: Netty Handler
 */
@Slf4j
public class NettyHandler extends ChannelDuplexHandler {
    private RemotingDataProcessor remotingDataProcessor;
    ExecutorService executorService;
    ;

    public NettyHandler(int corePoolSize,
                        int maximumPoolSize, RemotingDataProcessor remotingDataProcessor) {
        this.remotingDataProcessor = remotingDataProcessor;
        executorService = new ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                new NamedThreadFactory("tianai-rpc", true),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executorService.execute(new NettyRunnable(ctx, msg, remotingDataProcessor));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        NettyChannelAdapter channelAdapter = new NettyChannelAdapter(ctx.channel());
        remotingDataProcessor.writeMessage(channelAdapter, msg, ctx);
        super.write(ctx, msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.warn("netty IO异常: e=" + cause.getMessage());
        } else {
            cause.printStackTrace();
        }
        ctx.close();
        super.exceptionCaught(ctx, cause);
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



