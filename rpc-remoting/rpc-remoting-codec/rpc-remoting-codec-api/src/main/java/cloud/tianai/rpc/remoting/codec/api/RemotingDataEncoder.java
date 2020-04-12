package cloud.tianai.rpc.remoting.codec.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 12:33
 * @Description: 加密器
 */
public interface RemotingDataEncoder {

    /**
     * 编码方法
     * @param msg 待编码的对象
     * @return 变化后转换成的字节数组
     */
    byte[] encode(Object msg);
}
