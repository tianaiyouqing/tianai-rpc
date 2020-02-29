package cloud.tianai.remoting.netty;

import cloud.tianai.remoting.api.RemotingDataProcessor;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Object> {

    private RemotingDataEncoder encoder;
    private RemotingDataProcessor dataProcessor;

    public NettyEncoder(RemotingDataEncoder encoder, RemotingDataProcessor dataProcessor) {
        this.encoder = encoder;
        this.dataProcessor = dataProcessor;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        try {
            byte[] bytes = encoder.encode(msg);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        } catch (Exception ex) {
            if (dataProcessor.support(msg)) {
                dataProcessor.sendError(new NettyChannelAdapter(ctx.channel()), ex, msg);
            }else {
                throw ex;
            }
        }
    }
}
