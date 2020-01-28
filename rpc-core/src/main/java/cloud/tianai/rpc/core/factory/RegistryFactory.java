package cloud.tianai.rpc.core.factory;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.registory.api.Registry;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class RegistryFactory {

    private static Map<String, Registry> registryCache = new HashMap<>(2);
    private static Map<String, Class<? extends Registry>> registryClassMap = new HashMap<>(2);
    private static Object lock = new Object();

    static {
        try {
            addRegistry("zookeeper", "cloud.tianai.rpc.registry.zookeeper.ZookeeperRegistry");
        } catch (ClassNotFoundException e) {

        }
    }

    public static void addRegistry(String protocol, Class<? extends Registry> registryClass) {
        registryClassMap.remove(protocol);
        registryClassMap.put(protocol, registryClass);
    }

    public static void addRegistry(String protocol, String registryClassStr) throws ClassNotFoundException {
        Class<?> clazz = ClassUtils.forName(registryClassStr);
        if (!Registry.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("注册工厂必须实现[Registry]接口");
        }

        //noinspection unchecked
        addRegistry(protocol, (Class<? extends Registry>) clazz);
    }

    public static Registry getRegistry(URL url) {
        Registry res;
        String flag = getFlag(url);
        if ((res = registryCache.get(flag)) != null) {
            return res;
        }
        synchronized (lock) {
            if ((res = registryCache.get(flag)) != null) {
                return res;
            }
            String protocol = url.getProtocol();
            Class<? extends Registry> registryClass = registryClassMap.get(protocol);
            if (registryClass != null) {
                try {
                    res = ClassUtils.createObject(registryClass);
                    // 启动
                    res.start(url);
                    registryCache.remove(flag);
                    registryCache.put(flag, res);
                } catch (Exception e) {
                    throw new IllegalArgumentException("创建registry异常" + e.getMessage());
                }
                registryClassMap.remove(protocol);
            }
            return res;
        }
    }

    private static String getFlag(URL url) {
        return url.toString();
    }

}
