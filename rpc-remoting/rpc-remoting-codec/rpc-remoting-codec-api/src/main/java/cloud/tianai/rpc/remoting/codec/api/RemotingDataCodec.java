package cloud.tianai.rpc.remoting.codec.api;

import cloud.tianai.rpc.common.extension.SPI;

@SPI
public interface RemotingDataCodec {

    RemotingDataEncoder getEncoder();

    RemotingDataDecoder getDecoder();
}
