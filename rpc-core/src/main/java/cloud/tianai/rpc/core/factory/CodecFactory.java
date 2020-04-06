package cloud.tianai.rpc.core.factory;

import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataCodec;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/25 18:04
 * @Description: 编码解码器创建工厂
 */
public class CodecFactory {

    private static Map<String, RemotingDataCodec> codecCache = new ConcurrentHashMap<>(2);
    private static Map<String, Class<? extends RemotingDataCodec>> codecClassMap = new HashMap<>(2);

    static {
        // 加载SPI
        ExtensionLoader<RemotingDataCodec> extensionLoader = ExtensionLoader.getExtensionLoader(RemotingDataCodec.class);
        Map<String, Class<? extends RemotingDataCodec>> extensionClasses = extensionLoader.getExtensionClasses();
        extensionClasses.forEach(CodecFactory::registerCodec);
    }

    public static void registerCodec(String protocol, String codecClassStr) throws ClassNotFoundException {
        Class<?> codecClass = ClassUtils.forName(codecClassStr);
        if (!RemotingDataCodec.class.isAssignableFrom(codecClass)) {
            // 不是encoder
            throw new IllegalArgumentException("传入的 codec 必须实现 [RemotingDataCodec] 接口");
        }

        Class<? extends RemotingDataCodec> codecClassCast = (Class<? extends RemotingDataCodec>) codecClass;
        registerCodec(protocol, codecClassCast);
    }

    public static void registerCodec(String protocol,Class<? extends RemotingDataCodec> codecClass) {
        codecClassMap.remove(protocol);
        codecClassMap.put(protocol, codecClass);
    }

    public static RemotingDataCodec getCodec(String protocol) {
        RemotingDataCodec result = codecCache.computeIfAbsent(protocol, (p) -> {
            Class<? extends RemotingDataCodec> codecClass = codecClassMap.get(protocol);
            RemotingDataCodec res = null;
            if (codecClass != null) {
                try {
                    res = createCodec(protocol, codecClass);
                    codecClassMap.remove(protocol);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return res;
        });
        return result;
    }

    private static RemotingDataCodec createCodec(String protocol,
                                                 Class<? extends RemotingDataCodec> codecClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RemotingDataCodec res;
        if (!Objects.isNull(res = codecCache.get(protocol))) {
            return res;
        }
        res = ClassUtils.createObject(codecClass);
        return res;
    }

}
