package cloud.tianai.remoting.netty;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class NettyDecoder extends ReplayingDecoder<ByteBuf> {

    private RemotingDataDecoder decode;

    public NettyDecoder(RemotingDataDecoder decode) {
        this.decode = decode;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readInt()];
        in.readBytes(bytes);
        Object decode = null;
        try {
            decode = this.decode.decode(bytes, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.add(decode);
    }
}
