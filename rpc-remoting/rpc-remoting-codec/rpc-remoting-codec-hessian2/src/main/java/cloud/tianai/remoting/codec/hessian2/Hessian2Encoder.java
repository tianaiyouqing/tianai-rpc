package cloud.tianai.remoting.codec.hessian2;

import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import cloud.tianai.rpc.remoting.codec.api.exception.CodecException;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 16:57
 * @Description: 基于hessian的编码器
 */
public class Hessian2Encoder implements RemotingDataEncoder {
    @Override
    public byte[] encode(Object msg) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output ho = new Hessian2Output(os);
            ho.writeObject(msg);
            ho.getBytesOutputStream().flush();
            ho.completeMessage();
            ho.close();
            byte[] result = os.toByteArray();
            return result;
        } catch (IOException e) {
            throw new CodecException(e);
        }
    }
}
