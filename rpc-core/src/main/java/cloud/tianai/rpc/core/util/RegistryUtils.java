package cloud.tianai.rpc.core.util;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.extension.ExtensionLoader;
import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.registory.api.Registry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryUtils {

    public static Registry createAndStart(URL config) {
        ExtensionLoader<Registry> extensionLoader = ExtensionLoader.getExtensionLoader(Registry.class);
        // 创建一个新的 registry， 不从缓存读取，直接创建
        Registry registry = extensionLoader.createExtension(config.getProtocol(), false);
        // 重试次数
        registry.start(config);
        return registry;
    }
}
