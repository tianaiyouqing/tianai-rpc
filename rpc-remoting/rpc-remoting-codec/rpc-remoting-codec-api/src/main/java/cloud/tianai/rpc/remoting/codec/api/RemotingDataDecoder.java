package cloud.tianai.rpc.remoting.codec.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 12:34
 * @Description: 解码器
 */
public interface RemotingDataDecoder {

   /**
    * 解码
    * @param data byte数组
    * @param clazz 要解码后转换成的class
    * @param <T> 泛型
    * @return 具体的对象
    */
   <T> T decode(byte[] data, Class<T> clazz);
}
