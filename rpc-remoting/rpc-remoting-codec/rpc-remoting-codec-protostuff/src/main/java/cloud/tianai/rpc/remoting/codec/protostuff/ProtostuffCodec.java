package cloud.tianai.rpc.remoting.codec.protostuff;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;

public class ProtostuffCodec  implements RemotingDataCodec {

    private ProtostuffEncoder encoder;
    private ProtostuffDecoder decoder;

    public ProtostuffCodec() {
        this.encoder = new ProtostuffEncoder();
        this.decoder = new ProtostuffDecoder();
    }

    @Override
    public RemotingDataEncoder getEncoder() {
        return encoder;
    }

    @Override
    public RemotingDataDecoder getDecoder() {
        return decoder;
    }
}
