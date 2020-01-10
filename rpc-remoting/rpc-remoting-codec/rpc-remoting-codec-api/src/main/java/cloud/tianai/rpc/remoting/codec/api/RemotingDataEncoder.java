package cloud.tianai.rpc.remoting.codec.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 12:33
 * @Description: 加密器
 */
public interface RemotingDataEncoder {

    byte[] encode(Object msg);
}
