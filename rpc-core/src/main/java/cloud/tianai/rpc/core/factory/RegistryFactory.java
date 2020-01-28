package cloud.tianai.rpc.core.factory;

import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.registory.api.Registry;

import java.util.HashMap;
import java.util.Map;

public class RegistryFactory {

    private static Map<String, Class<? extends Registry>> registryClassMap = new HashMap<>(2);

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

    public static Registry createRegistry(String protocol) {
        Class<? extends Registry> registryClass = registryClassMap.get(protocol);
        if (registryClass == null) {
            throw new IllegalArgumentException("无法找到对应的registry， protocol=" + protocol);
        }
        return createRegistry(registryClass);
    }

    public static Registry createRegistry(Class<? extends Registry> registryClass) {
        try {
            return ClassUtils.createObject(registryClass);
        } catch (Exception e) {
            throw new IllegalArgumentException("创建registry异常" + e.getMessage());
        }
    }
}
