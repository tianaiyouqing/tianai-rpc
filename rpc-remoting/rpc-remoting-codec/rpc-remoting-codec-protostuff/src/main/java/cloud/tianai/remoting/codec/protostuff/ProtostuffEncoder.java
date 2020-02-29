package cloud.tianai.remoting.codec.protostuff;

import cloud.tianai.remoting.codec.protostuff.utils.WrapperUtils;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import cloud.tianai.rpc.remoting.codec.api.exception.CodecException;
import io.protostuff.GraphIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/29 19:31
 * @Description: Protostuff 编码器
 */
public class ProtostuffEncoder implements RemotingDataEncoder {

    /**
     * 编码器
     * @param obj 要编码的对象
     * @return
     */
    @Override
    public byte[] encode(Object obj) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        LinkedBuffer buffer = LinkedBuffer.allocate();
        byte[] bytes;
        byte[] classNameBytes;
        try {
            if (obj == null || WrapperUtils.needWrapper(obj)) {
                Schema<Wrapper> schema = RuntimeSchema.getSchema(Wrapper.class);
                Wrapper wrapper = new Wrapper(obj);
                bytes = GraphIOUtil.toByteArray(wrapper, schema, buffer);
                classNameBytes = Wrapper.class.getName().getBytes();
            } else {
                Schema schema = RuntimeSchema.getSchema(obj.getClass());
                bytes = GraphIOUtil.toByteArray(obj, schema, buffer);
                classNameBytes = obj.getClass().getName().getBytes();
            }
        } finally {
            buffer.clear();
        }

        try {
            dos.writeInt(classNameBytes.length);
            dos.writeInt(bytes.length);
            dos.write(classNameBytes);
            dos.write(bytes);
            byte[] result = os.toByteArray();
            return result;
        } catch (IOException e) {
            throw new CodecException(e);
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                // 不做处理
            }
        }
    }
}
