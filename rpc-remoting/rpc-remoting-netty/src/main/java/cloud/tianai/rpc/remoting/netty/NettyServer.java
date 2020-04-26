package cloud.tianai.rpc.remoting.netty;

import cloud.tianai.rpc.common.threadpool.NamedThreadFactory;
import cloud.tianai.rpc.remoting.api.AbstractRemotingServer;
import cloud.tianai.rpc.remoting.api.RemotingChannelHolder;
import cloud.tianai.rpc.remoting.api.RemotingServerConfiguration;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 15:40
 * @Description: 基于netty实现的远程通讯框架
 */
@Slf4j
public class NettyServer extends AbstractRemotingServer {

    public static final String SERVER_TYPE = "NETTY_SERVER";
    public static final Integer DEFAULT_PORT = 20880;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private InetSocketAddress address;
    private NettyRemotingChannelHolder channelHolder;
    /**
     * 线程池.
     */
    ExecutorService threadPool;

    @Override
    public RemotingChannelHolder doStart() throws RpcRemotingException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // 创建工作线程池，如果有必要
        initThreadLocal();

        // 创建eventLoopGroup
        initEventLoopGroup();

        // 包装bootstrap
        warpBootStrap(serverBootstrap);

        // 绑定端口
        ChannelFuture channelFuture = bind(serverBootstrap);

        channelFuture.syncUninterruptibly();
        if (channelFuture.isSuccess()) {
            if (log.isInfoEnabled()) {
                log.info("[tianai-rpc] - Netty start, address[{}]", channelFuture.channel().localAddress());
            } else {
                System.out.println("[tianai-rpc] - Netty start, address[" + channelFuture.channel().localAddress() + "]");
            }
        }
        channel = channelFuture.channel();
        channelHolder = NettyRemotingChannelHolder.create(channel);
        return channelHolder;
    }

    private void initThreadLocal() {
        // 创建默认线程池
        this.threadPool = new ThreadPoolExecutor(200,
                200,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                new NamedThreadFactory("tianai-rpc-svc", false),
                new ThreadPoolExecutor.AbortPolicy());
    }


    private ChannelFuture bind(ServerBootstrap serverBootstrap) {
        String host = getUrl().getHost();
        Integer port = getUrl().getPort();
        if (port < 1) {
            port = DEFAULT_PORT;
        }
        if (StringUtils.isBlank(host)) {
            address = new InetSocketAddress(port);
        } else {
            address = new InetSocketAddress(host, port);
        }
        return serverBootstrap.bind(address);
    }

    private void warpBootStrap(ServerBootstrap serverBootstrap) {
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("Encoder", new NettyEncoder(getRemotingDataCodec(), getRemotingDataProcessor()));
                        pipeline.addLast("Decoder", new NettyDecoder(getRemotingDataCodec()));
                        pipeline.addLast("server-idle-handler",
                                new IdleStateHandler(0, 0, getServerIdleTimeout(), MILLISECONDS));
                        pipeline.addLast("handler", new NettyServerHandler(threadPool, getRemotingDataProcessor()));
                    }
                });
    }

    private void initEventLoopGroup() {
        if (bossGroup != null && workerGroup != null) {
            return;
        }
        // 创建 eventLoopGroup
        if (Epoll.isAvailable()) {
            // epoll
            bossGroup = new EpollEventLoopGroup(getBossThreads());
            workerGroup = new EpollEventLoopGroup(getWorkerThreads());
        } else {
            bossGroup = new NioEventLoopGroup(getBossThreads());
            workerGroup = new NioEventLoopGroup(getWorkerThreads());
        }

    }

    @Override
    public void doDestroy() throws RpcRemotingException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
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
        return SERVER_TYPE;
    }
}
