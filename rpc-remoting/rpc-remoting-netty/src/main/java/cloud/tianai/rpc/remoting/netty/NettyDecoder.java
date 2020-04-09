package cloud.tianai.rpc.remoting.netty;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NettyDecoder extends ReplayingDecoder<ByteBuf> {

    private RemotingDataCodec codec;

    public NettyDecoder(RemotingDataCodec codec) {
        this.codec = codec;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readInt()];
        try {
            in.readBytes(bytes);
            Object decode = this.codec.getDecoder().decode(bytes, Object.class);
            out.add(decode);
        } catch (Exception e) {
            // 解码异常, 直接打印异常日志
            if (log.isErrorEnabled()) {
                log.error("TIANAI-RPC NettyServerHandler , 解码异常, ex={}, data={}", e, bytes);
            }else {
                System.out.println("TIANAI-RPC NettyServerHandler , 解码异常, ex=" + e.getLocalizedMessage() + ", data=" +  bytes);
            }
            throw e;
        }
    }
}
