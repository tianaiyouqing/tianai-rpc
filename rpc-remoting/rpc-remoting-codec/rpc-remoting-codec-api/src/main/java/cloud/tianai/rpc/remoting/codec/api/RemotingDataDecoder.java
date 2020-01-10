package cloud.tianai.rpc.remoting.codec.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 12:34
 * @Description: 解码器
 */
public interface RemotingDataDecoder {

   <T> T decode(byte[] data, Class<T> clazz);
}
