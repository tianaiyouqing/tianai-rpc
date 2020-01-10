package cloud.tianai.remoting.netty;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Object> {

    private RemotingDataEncoder encoder;

    public NettyEncoder(RemotingDataEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] bytes = new byte[0];
        try {
            bytes = encoder.encode(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
