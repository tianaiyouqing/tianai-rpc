package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.AbstractRemotingClient;
import cloud.tianai.remoting.api.RemotingChannelHolder;
import cloud.tianai.remoting.api.RemotingConfiguration;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 17:42
 * @Description: 基于netty的client
 */
public class NettyClient extends AbstractRemotingClient {
    public static final String REMOTING_TYPE = "NETTY";

    private Channel channel;
    private EventLoopGroup workerGroup;
    private NettyRemotingChannelHolder channelHolder;

    @Override
    public RemotingChannelHolder doStart(RemotingConfiguration config) throws RpcRemotingException {
        Bootstrap bs = new Bootstrap();
        // 初始化eventLoopGroup
        initEventLoopGroup(config);

        // 包装bootstrap
        warpBootStrap(bs, config);

        // 链接
        this.channel = connect(bs, config);

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
                        pipeline.addLast("Encoder", new NettyEncoder(config.getEncoder()));
                        pipeline.addLast("Decoder", new NettyDecoder(config.getDecoder()));
                        pipeline.addLast("handler", new NettyHandler(config.getExecuteThreads(), config.getExecuteThreads(), config.getRemotingDataProcessor()));
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
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public RemotingChannelHolder getchannel() {
        return channelHolder;
    }

    @Override
    public String getRemotingType() {
        return REMOTING_TYPE;
    }
}
