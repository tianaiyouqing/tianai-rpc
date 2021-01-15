package cloud.tianai.rpc.remoting.netty;

import cloud.tianai.rpc.remoting.api.RemotingDataProcessor;
import cloud.tianai.rpc.common.exception.ServiceNotSupportedException;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NettyDecoder extends ReplayingDecoder<ByteBuf> {

    private RemotingDataCodec codec;
    private RemotingDataProcessor dataProcessor;

    public NettyDecoder(RemotingDataCodec codec, RemotingDataProcessor dataProcessor) {
        this.codec = codec;
        this.dataProcessor = dataProcessor;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readInt()];
        try {
            in.readBytes(bytes);
            Object decode = this.codec.getDecoder().decode(bytes, Object.class);
            out.add(decode);
        } catch (Exception ex) {
            if (!(ex instanceof ServiceNotSupportedException)) {
                // 包装成服务不支持异常
                ex = new ServiceNotSupportedException(ex);
            }
            dataProcessor.sendError(new NettyChannelAdapter(ctx.channel()), ex, in);
        }
    }
}
