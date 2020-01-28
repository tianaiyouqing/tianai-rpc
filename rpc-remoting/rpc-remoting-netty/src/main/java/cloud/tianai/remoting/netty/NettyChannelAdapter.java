package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.Channel;
import cloud.tianai.rpc.common.exception.RpcException;
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
    public Object getUnsafe() {
        return unsafe;
    }
}
