package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.AbstractRemotingClient;
import cloud.tianai.remoting.api.RemotingChannelHolder;
import cloud.tianai.remoting.api.RemotingConfiguration;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.common.util.ThreadUtils;
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
    private RemotingConfiguration config;

    @Override
    public RemotingChannelHolder doStart(RemotingConfiguration config) throws RpcRemotingException {
        bootstrap = new Bootstrap();
        this.config = config;
        // 初始化eventLoopGroup
        initEventLoopGroup(config);
        // 包装bootstrap
        warpBootStrap(bootstrap, config);

        // 链接
        this.channel = connect(bootstrap, config);

        channelHolder = NettyRemotingChannelHolder.create(this.channel);
        return channelHolder;
    }

    private Channel connect(Bootstrap bs, RemotingConfiguration config) {
        ChannelFuture channelFuture = bs.connect(new InetSocketAddress(config.getHost(), config.getPort()));
        boolean ret = channelFuture.awaitUninterruptibly(config.getConnectTimeout(), MILLISECONDS);
        if (ret && channelFuture.isSuccess()) {
            channel = channelFuture.channel();
            return channel;
        }
        throw new RpcRemotingException("client链接超时," +
                " host=[" + config.getHost() + "]," +
                " port=[" + config.getPort() + "]," +
                " 超时时间:" + config.getConnectTimeout() + MILLISECONDS.toString()
        );
    }

    private void warpBootStrap(Bootstrap bs, RemotingConfiguration config) {
        bs.channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .group(workerGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.max(3000, config.getConnectTimeout()))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("Encoder", new NettyEncoder(config.getCodec(), config.getRemotingDataProcessor()));
                        pipeline.addLast("Decoder", new NettyDecoder(config.getCodec()));
                        // client 处理只读心跳
                        pipeline.addLast("client-idle-handler",
                                new IdleStateHandler(config.getIdleTimeout(), 0, 0, MILLISECONDS));
                        pipeline.addLast("handler", new NettyClientHandler(config.getRemotingDataProcessor()));
                    }
                });
    }

    private void initEventLoopGroup(RemotingConfiguration config) {
        if (workerGroup == null) {
            if (Epoll.isAvailable()) {
                workerGroup = new EpollEventLoopGroup(config.getWorkerThreads());
            } else {
                workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
            }
        }
    }

    @Override
    public void doStop() throws RpcRemotingException {
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
        Channel channel = connect(this.bootstrap, config);
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
