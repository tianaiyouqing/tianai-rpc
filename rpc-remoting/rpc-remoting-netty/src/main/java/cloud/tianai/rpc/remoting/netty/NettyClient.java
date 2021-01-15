package cloud.tianai.rpc.remoting.netty;

import cloud.tianai.rpc.common.util.ThreadUtils;
import cloud.tianai.rpc.remoting.api.AbstractRemotingClient;
import cloud.tianai.rpc.remoting.api.RemotingChannelHolder;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 17:42
 * @Description: 基于netty的client
 */
@Slf4j
public class NettyClient extends AbstractRemotingClient {
    public static final String REMOTING_TYPE = "NETTY";

    private Channel channel;
    private EventLoopGroup workerGroup;
    private NettyRemotingChannelHolder channelHolder;
    private Bootstrap bootstrap;

    @Override
    public RemotingChannelHolder doStart() throws RpcRemotingException {
        bootstrap = new Bootstrap();
        // 初始化eventLoopGroup
        initEventLoopGroup();
        // 包装bootstrap
        warpBootStrap(bootstrap);

        // 链接
        this.channel = connect(bootstrap);

        channelHolder = NettyRemotingChannelHolder.create(this.channel);
        return channelHolder;
    }

    private Channel connect(Bootstrap bs) {
        ChannelFuture channelFuture = bs.connect(new InetSocketAddress(getUrl().getHost(), getUrl().getPort()));
        boolean ret = channelFuture.awaitUninterruptibly(getConnectTimeout(), MILLISECONDS);
        if (ret && channelFuture.isSuccess()) {
            channel = channelFuture.channel();
            return channel;
        }
        throw new RpcRemotingException("client链接超时," +
                " host=[" + getUrl().getHost() + "]," +
                " port=[" + getUrl().getPort() + "]," +
                " 超时时间:" + getConnectTimeout() + MILLISECONDS.toString()
        );
    }

    private void warpBootStrap(Bootstrap bs) {
        bs.channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .group(workerGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.max(3000, getConnectTimeout()))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("Encoder", new NettyEncoder(getRemotingDataCodec(), getRemotingDataProcessor()));
                        pipeline.addLast("Decoder", new NettyDecoder(getRemotingDataCodec(), getRemotingDataProcessor()));
                        // client 处理只读心跳
                        pipeline.addLast("client-idle-handler",
                                new IdleStateHandler(getIdleTimeout(), 0, 0, MILLISECONDS));
                        pipeline.addLast("handler", new NettyClientHandler(getRemotingDataProcessor()));
                    }
                });
    }

    private void initEventLoopGroup() {
        if (workerGroup == null) {
            if (Epoll.isAvailable()) {
                workerGroup = new EpollEventLoopGroup(getWorkerThreads());
            } else {
                workerGroup = new NioEventLoopGroup(getWorkerThreads());
            }
        }
    }

    @Override
    public void doDestroy() throws RpcRemotingException {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public RemotingChannelHolder getChannel() {
        return channelHolder;
    }

    @Override
    public String getRemotingType() {
        return REMOTING_TYPE;
    }

    @Override
    public void doConnect() {
        if (this.channel != null && this.channel.isOpen()) {
            // 关闭channel
            log.info("关闭channel进行重连....");
            this.channel.disconnect();
        }

        // 连接channel
        Channel channel = connect(this.bootstrap);
        log.info("连接channel:" + channel.remoteAddress());
        this.channelHolder.setChannel(channel);
    }

    @Override
    public void reconnect(int retryCount) throws TimeoutException {
        reconnect(0, Math.max(retryCount, 1));
    }


    protected void reconnect(int currRetryCount, int retryCount) throws TimeoutException {
        try {
            doConnect();
        } catch (RpcRemotingException ex) {
            // 这里应该进行重试
            if (currRetryCount < retryCount) {
                // 休眠100毫秒
                ThreadUtils.sleep(100, MILLISECONDS);
                reconnect(++currRetryCount, retryCount);
            } else {
                throw new TimeoutException(ex.getMessage());
            }
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        if (channel != null) {
            return channel.remoteAddress();
        }
        return null;
    }
}
