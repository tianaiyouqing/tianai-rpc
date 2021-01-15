package cloud.tianai.rpc.remoting.netty;

import cloud.tianai.rpc.remoting.api.Channel;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;
import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 21:44
 * @Description: Netty的channel适配器
 */
public class NettyChannelAdapter implements Channel {

    private io.netty.channel.Channel unsafe;

    public NettyChannelAdapter(io.netty.channel.Channel unsafe) {
        this.unsafe = unsafe;
    }

    @Override
    public void write(Object obj) {
        ChannelFuture future = unsafe.writeAndFlush(obj);
        Throwable cause = future.cause();
        if(cause != null) {
            throw new RpcRemotingException(cause);
        }
    }

    @Override
    public SocketAddress getLocalAddress() {
        return unsafe.localAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return unsafe.remoteAddress();
    }

    @Override
    public void close() {
        // 关闭通道，不间断等待
//        this.unsafe.close().awaitUninterruptibly();
        // 异步关闭通道
        this.unsafe.close();
    }

    @Override
    public Object getUnsafe() {
        return unsafe;
    }
}
