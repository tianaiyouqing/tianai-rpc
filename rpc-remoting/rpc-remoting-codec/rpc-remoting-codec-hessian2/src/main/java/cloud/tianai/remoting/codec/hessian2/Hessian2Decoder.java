package cloud.tianai.remoting.codec.hessian2;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.exception.CodecException;
import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 17:02
 * @Description: 基于hessian2的Decode反序列化
 */
public class Hessian2Decoder implements RemotingDataDecoder {
    @Override
    public <T> T decode(byte[] data, Class<T> clazz) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            Hessian2Input hessian2Input = new Hessian2Input(in);
            Object obj = hessian2Input.readObject(clazz);
            return (T) obj;
        } catch (IOException e) {
            throw new CodecException(e);
        }
    }
}
