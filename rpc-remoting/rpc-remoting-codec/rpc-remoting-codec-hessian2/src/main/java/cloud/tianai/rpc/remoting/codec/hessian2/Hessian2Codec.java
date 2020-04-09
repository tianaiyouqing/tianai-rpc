package cloud.tianai.rpc.remoting.codec.hessian2;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;


public class Hessian2Codec implements RemotingDataCodec {

    private Hessian2Encoder encoder;
    private Hessian2Decoder decoder;

    public Hessian2Codec() {
        this.encoder = new Hessian2Encoder();
        this.decoder = new Hessian2Decoder();
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
