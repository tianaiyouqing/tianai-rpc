package cloud.tianai.rpc.remoting.codec.api;

import cloud.tianai.rpc.common.extension.SPI;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/12 22:05
 * @Description: 序列化，包含编码器和解码器
 */
@SPI
public interface RemotingDataCodec {

    /**
     * 获取编码器
     * @return RemotingDataEncoder
     */
    RemotingDataEncoder getEncoder();


    /**
     * 获取解码器
     * @return RemotingDataDecoder
     */
    RemotingDataDecoder getDecoder();
}
