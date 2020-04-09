package cloud.tianai.rpc.remoting.codec.protostuff;

import cloud.tianai.rpc.remoting.codec.protostuff.utils.WrapperUtils;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.exception.CodecException;
import io.protostuff.GraphIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/29 19:40 
 * @Description: Protostuff 解码器
 */
public class ProtostuffDecoder implements RemotingDataDecoder {
    @Override
    public <T> T decode(byte[] data, Class<T> clazz) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            Object decode = decode(in);
            return (T)decode;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // 不做处理
            }
        }
    }


    private Object decode(InputStream in) {
        DataInputStream dis = new DataInputStream(in);
        try {
            int classNameLength = dis.readInt();
            int bytesLength = dis.readInt();
            if (classNameLength < 0 || bytesLength < 0) {
                throw new CodecException();
            }
            byte[] classNameBytes = new byte[classNameLength];
            dis.readFully(classNameBytes, 0, classNameLength);

            byte[] bytes = new byte[bytesLength];
            dis.readFully(bytes, 0, bytesLength);

            String className = new String(classNameBytes);
            Class clazz = Class.forName(className);

            Object result;
            if (WrapperUtils.needWrapper(clazz)) {
                Schema<Wrapper> schema = RuntimeSchema.getSchema(Wrapper.class);
                Wrapper wrapper = schema.newMessage();
                GraphIOUtil.mergeFrom(bytes, wrapper, schema);
                result = wrapper.getData();
            } else {
                Schema schema = RuntimeSchema.getSchema(clazz);
                result = schema.newMessage();
                GraphIOUtil.mergeFrom(bytes, result, schema);
            }
            return  result;
        } catch (IOException | ClassNotFoundException e) {
            throw new CodecException(e);
        } finally {
            try {
                dis.close();
            } catch (IOException e) {
                // 不做处理
            }
        }
    }
}
