package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.*;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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
    private RemotingServerConfiguration remotingServerConfiguration;

    @Override
    public RemotingChannelHolder doStart(RemotingServerConfiguration config) throws RpcRemotingException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // 创建eventLoopGroup
        initEventLoopGroup(config);

        // 包装bootstrap
        warpBootStrap(serverBootstrap, config);

        // 绑定端口
        ChannelFuture channelFuture = bind(serverBootstrap, config);

        channelFuture.syncUninterruptibly();
        if (channelFuture.isSuccess()) {
            if (log.isInfoEnabled()) {
                log.info("[tianai-rpc] - Netty start, address[{}]", channelFuture.channel().localAddress());
            }else {
                System.out.println("[tianai-rpc] - Netty start, address["+ channelFuture.channel().localAddress() +"]");
            }
        }
        channel = channelFuture.channel();
        channelHolder = NettyRemotingChannelHolder.create(channel);
        this.remotingServerConfiguration = config;
        return channelHolder;
    }


    private ChannelFuture bind(ServerBootstrap serverBootstrap, RemotingServerConfiguration config) {
        String host = config.getHost();
        Integer port;
        if (config.getPort() == null || config.getPort() < 1) {
            port = DEFAULT_PORT;
        } else {
            port = config.getPort();
        }
        if (StringUtils.isBlank(config.getHost())) {
            address = new InetSocketAddress(port);
        } else {
            address = new InetSocketAddress(host, port);
        }
        return serverBootstrap.bind(address);
    }

    private void warpBootStrap(ServerBootstrap serverBootstrap, RemotingServerConfiguration config) {
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("Encoder", new NettyEncoder(config.getEncoder()));
                        pipeline.addLast("Decoder", new NettyDecoder(config.getDecoder()));
                        pipeline.addLast("server-idle-handler",
                                new IdleStateHandler(0, 0, config.getIdleTimeout(), MILLISECONDS));
                        pipeline.addLast("handler", new NettyHandler(config.getExecuteThreads(),
                                config.getExecuteThreads(),
                                config.getRemotingDataProcessor()));
                    }
                });
    }

    private void initEventLoopGroup(RemotingServerConfiguration config) {
        if (bossGroup != null && workerGroup != null) {
            return;
        }
        // 创建 eventLoopGroup
        if (Epoll.isAvailable()) {
            // epoll
            bossGroup = new EpollEventLoopGroup(config.getBossThreads());
            workerGroup = new EpollEventLoopGroup(config.getWorkerThreads());
        } else {
            bossGroup = new NioEventLoopGroup(config.getBossThreads());
            workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
        }

    }

    @Override
    public void doStop() throws RpcRemotingException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public RemotingChannelHolder getchannel() {
        return channelHolder;
    }

    @Override
    public String getRemotingType() {
        return SERVER_TYPE;
    }

    @Override
    public RemotingServerConfiguration getRemotingServerConfiguration() {
        return remotingServerConfiguration;
    }
}
