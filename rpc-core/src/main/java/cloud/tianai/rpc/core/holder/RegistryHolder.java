package cloud.tianai.rpc.core.holder;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.registory.api.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 18:17
 * @Description: 注册工厂持有者
 */
public class RegistryHolder {
    private static Map<String, Registry> registryCache = new HashMap<>(2);
    private static final Object LOCK = new Object();

    static {
        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(RegistryHolder::shutdown));
    }

    public static Registry getRegistry(URL url) {
        String key = getKey(url);
        return registryCache.get(key);
    }

    public static Registry getRegistry(String key) {
        return registryCache.get(key);
    }

    public static void putRegistry(URL url, Registry registry) {
        String key = getKey(url);
        Registry oldRegistry = registryCache.remove(key);
        registryCache.put(key, registry);
        if (oldRegistry != null) {
            // 删除旧的registry
            oldRegistry.shutdown();
        }
    }

    public static void putRegistry(String key, Registry registry) {
        Registry oldRegistry = registryCache.remove(key);
        registryCache.put(key, registry);
        if (oldRegistry != null) {
            // 删除旧的registry
            oldRegistry.shutdown();
        }
    }

    public static Registry computeIfAbsent(URL url, Supplier<Registry> supplier) {
        String key = getKey(url);
        Registry registry = getRegistry(key);
        if (registry == null) {
            synchronized (LOCK) {
                if ((registry = getRegistry(key)) == null) {
                    registry = supplier.get();
                    putRegistry(key, registry);
                }
            }
        }
        return registry;
    }

    public static void remove(String key) {
        Registry registry = registryCache.remove(key);
        if (registry != null) {
            registry.shutdown();
        }
    }

    public static void remove(URL url) {
        String key = getKey(url);
        Registry registry = registryCache.remove(key);
        if (registry != null) {
            registry.shutdown();
        }
    }

    public static void shutdown() {
        for (String key : registryCache.keySet()) {
            remove(key);
        }
    }

    private static String getKey(URL url) {
        return url.toString();
    }

}
