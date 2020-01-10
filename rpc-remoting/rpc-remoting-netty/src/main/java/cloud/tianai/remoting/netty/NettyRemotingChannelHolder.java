package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.DefaultFuture;
import cloud.tianai.remoting.api.RemotingChannelHolder;
import cloud.tianai.remoting.api.Request;
import io.netty.channel.Channel;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 16:27
 * @Description: Netty实现的远程channelHolder
 */
public class NettyRemotingChannelHolder implements RemotingChannelHolder  {

    private Channel nettyChannel;
    private cloud.tianai.remoting.api.Channel channel;

    public NettyRemotingChannelHolder(Channel nettyChannel) {
        this.nettyChannel = nettyChannel;
        this.channel = new NettyChannelAdapter(nettyChannel);
    }


    public static NettyRemotingChannelHolder create(Channel channel) {
        NettyRemotingChannelHolder channelHolder = new NettyRemotingChannelHolder(channel);
        return channelHolder;
    }
    @Override
    public String getChannelType() {
        return NettyServer.SERVER_TYPE;
    }

    @Override
    public cloud.tianai.remoting.api.Channel getNettyChannel() {
        return channel;
    }

    @Override
    public CompletableFuture<Object> request(Request request, int timeout) {
        DefaultFuture future = DefaultFuture.newFuture(channel, request, timeout);
        // 写数据
        nettyChannel.writeAndFlush(request);
        return future;
    }
}
