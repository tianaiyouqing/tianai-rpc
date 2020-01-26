package cloud.tianai.rpc.core.server.remoting;

import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/25 18:04
 * @Description: 编码解码器创建工厂
 */
public class CodecFactory {

    private static Map<String, KeyValue<RemotingDataEncoder, RemotingDataDecoder>> codecCache = new HashMap<>(2);

    private static Map<String, KeyValue<Class<? extends RemotingDataEncoder>, Class<? extends RemotingDataDecoder>>> codecClassMap
            = new HashMap<>(2);


    static {
        // 注册一些默认的codec
        try {
            // 注册 hessian2
            registerCodec("hessian2",
                    "cloud.tianai.remoting.codec.hessian2.Hessian2Encoder",
                    "cloud.tianai.remoting.codec.hessian2.Hessian2Decoder");
        } catch (ClassNotFoundException e) {
            // 不做处理
        }
    }

    public static void registerCodec(String protocol, String encoderClassStr, String decoderClassStr) throws ClassNotFoundException {

        Class<?> encoderClass = ClassUtils.forName(encoderClassStr);
        Class<?> decoderClass = ClassUtils.forName(decoderClassStr);

        if(!RemotingDataEncoder.class.isAssignableFrom(encoderClass)) {
            // 不是encoder
            throw new IllegalArgumentException("传入的encoder必须实现 [RemotingDataEncoder] 接口");
        }
        if(!RemotingDataDecoder.class.isAssignableFrom(decoderClass)) {
            // 不是decoder
            throw new IllegalArgumentException("传入的decoder必须实现 [RemotingDataDecoder] 接口");
        }

        Class<? extends RemotingDataEncoder> encoderClassCast = (Class<? extends RemotingDataEncoder>) encoderClass;
        Class<? extends RemotingDataDecoder> decoderClassCast = (Class<? extends RemotingDataDecoder>) decoderClass;
        registerCodec(protocol, encoderClassCast, decoderClassCast);
    }
    public static void registerCodec(String protocol,
                                     Class<? extends RemotingDataEncoder> encoderClass,
                                     Class<? extends RemotingDataDecoder> decoderClass) throws ClassNotFoundException {
        codecClassMap.remove(protocol);
        codecClassMap.put(protocol, new KeyValue<>(encoderClass, decoderClass));
    }

    public static KeyValue<RemotingDataEncoder, RemotingDataDecoder> getCodec(String protocol) {
        KeyValue<RemotingDataEncoder, RemotingDataDecoder> res;
        if (!Objects.isNull(res = codecCache.get(protocol))) {
            return res;
        }
        // todo 可能会导致创建多次，建议加锁
        KeyValue<Class<? extends RemotingDataEncoder>, Class<? extends RemotingDataDecoder>> codecClass = codecClassMap.get(protocol);
        if (codecClass != null && codecClass.isNotEmpty()) {
            Class<? extends RemotingDataEncoder> encoder = codecClass.getKey();
            Class<? extends RemotingDataDecoder> decoder = codecClass.getValue();
            try {
                res = getCodec(protocol, encoder, decoder);
                codecClassMap.remove(protocol);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static KeyValue<RemotingDataEncoder, RemotingDataDecoder> getCodec(String protocol,
                                                                              Class<? extends RemotingDataEncoder> encoderClass,
                                                                              Class<? extends RemotingDataDecoder> decoderClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyValue<RemotingDataEncoder, RemotingDataDecoder> res;
        if (!Objects.isNull(res = codecCache.get(protocol))) {
            return res;
        }
        RemotingDataEncoder encoder = ClassUtils.createObject(encoderClass);
        RemotingDataDecoder decoder = ClassUtils.createObject(decoderClass);

        res = new KeyValue<>(encoder, decoder);

        codecCache.remove(protocol);
        codecCache.put(protocol, res);
        return res;
    }


}
