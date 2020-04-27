package cloud.tianai.rpc.core.holder;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.registory.api.Registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 18:17
 * @Description: 服务注册持有者
 */
public class RegistryHolder {

    private static Map<String, Registry> registryCache = new ConcurrentHashMap<>(2);

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

    public static Registry computeIfAbsent(URL url, Function<URL, Registry> supplier) {
        String key = getKey(url);
        Registry registry = registryCache.computeIfAbsent(key, (k) -> supplier.apply(url));
        return registry;
    }

    public static void remove(String key) {
        Registry registry = registryCache.remove(key);
        if (registry != null) {
            registry.destroy();
        }
    }

    public static void remove(URL url) {
        String key = getKey(url);
        Registry registry = registryCache.remove(key);
        if (registry != null) {
            registry.destroy();
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
