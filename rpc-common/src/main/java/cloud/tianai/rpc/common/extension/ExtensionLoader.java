package cloud.tianai.rpc.common.extension;

import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.common.util.ExceptionUtils;
import cloud.tianai.rpc.common.util.Holder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;


/**
 * @Author: 天爱有情
 * @Date: 2020/04/06 13:05
 * @Description: SPI扩展加载器
 */
@Slf4j
public class ExtensionLoader<T> {

    /**
     * TIANAI-RPC扩展点默认路径.
     */
    private static final String RPC_DIRECTORY = "META-INF/tianai-rpc/";

    /**
     * 兼容JAVA扩展点默认路径 .
     */
    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    /**
     * 逗号匹配 正则表达式.
     */
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    /**
     * 存储 ExtensionLoader 的缓存集合.
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * 存储 Extension 的实例.
     */
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 异常集合.
     */
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    /**
     * 缓存实例集合.
     */
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    /**
     * 缓存的names.
     */
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();

    /**
     * 缓存的 class.
     */
    private final Holder<Map<String, Class<? extends T>>> cachedClasses = new Holder<>();

    /**
     * 当前指定的类型.
     */
    private final Class<?> type;

    /**
     * 缓存默认名称.
     */
    private String cachedDefaultName;


    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }


    /**
     * 通过 class 获取 扩展的名称
     *
     * @param extensionClass
     * @return
     */
    public String getExtensionName(Class<?> extensionClass) {
        getExtensionClasses();// load class
        return cachedNames.get(extensionClass);
    }

    /**
     * 是否有该扩展
     *
     * @param name 名称
     * @return boolean
     */
    public boolean hasExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        Class<?> c = this.getExtensionClass(name);
        return c != null;
    }

    /**
     * 通过名称 获取 对应的 class
     *
     * @param name 名称
     * @return Class<? extends T>
     */
    public Class<? extends T> getExtensionClass(String name) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Extension name == null");
        }
        return getExtensionClasses().get(name);
    }

    /**
     * 通过 名称 获取 扩展类
     *
     * @param name name
     * @return T
     * 如果获取不到，则抛出异常
     */
    public T getExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if (Boolean.TRUE.toString().equals(name)) {
            return getDefaultExtension();
        }
        final Holder<Object> holder = getOrCreateHolder(name);
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name, true);
                    holder.set(instance);
                }
            }
        }
        //noinspection unchecked
        return (T) instance;
    }

    /**
     * 创建 扩展 通过名称。 如果找不到，则抛出异常
     *
     * @param name name
     * @return T
     */
    @SuppressWarnings("unchecked")
    public T createExtension(String name, boolean cache) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance;
            if (cache) {
                instance = (T) EXTENSION_INSTANCES.get(clazz);
                if (instance == null) {
                    EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                    instance = (T) EXTENSION_INSTANCES.get(clazz);
                }
            } else {
                instance = (T) clazz.newInstance();
            }
            // 初始化
            initExtension(instance);
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance (name: " + name + ", class: " +
                    type + ") couldn't be instantiated: " + t.getMessage(), t);
        }
    }


    /**
     * 获取异常信息
     *
     * @param name name
     * @return IllegalStateException
     */
    public IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);


        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(ExceptionUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    /**
     * 获取默认的 Extension
     *
     * @return T
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName) || Boolean.TRUE.toString().equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    /**
     * 获取 扩展的所有 class
     *
     * @return Map<String, Class < ? extends T>>
     */
    public Map<String, Class<? extends T>> getExtensionClasses() {
        Map<String, Class<? extends T>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }


    /**
     * 通过API注册新的扩展程序
     * @param name 名称
     * @param clazz class类型
     */
    public void addExtension(String name, Class<?> clazz) {
        getExtensionClasses(); // load classes

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Input type " +
                    clazz + " doesn't implement the Extension " + type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("Input type " +
                    clazz + " can't be interface!");
        }

        @SuppressWarnings("unchecked")
        Class<? extends T> transformClass = (Class<? extends T>) clazz;

        if (StringUtils.isBlank(name)) {
            throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
        }
        if (cachedClasses.get().containsKey(name)) {
            throw new IllegalStateException("Extension name " +
                    name + " already exists (Extension " + type + ")!");
        }

        cachedNames.put(clazz, name);
        cachedClasses.get().put(name, transformClass);
    }


    /**
     * 初始化 扩展时的一个扩展接口
     *
     * @param instance
     */
    private void initExtension(T instance) {
        // 留一个扩展接口
    }


    /**
     * 获取或创建一个 对 扩展对象的包装器
     *
     * @param name name
     * @return Holder<Object>
     */
    private Holder<Object> getOrCreateHolder(String name) {
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        return holder;
    }




    /**
     * 加载 扩展的所有class
     *
     * @return Map<String, Class < ? extends T>>
     */
    private Map<String, Class<? extends T>> loadExtensionClasses() {
        cacheDefaultExtensionName();
        Map<String, Class<? extends T>> extensionClasses = new HashMap<>();
        // 读取 META-INF/tianai-rpc/
        loadDirectory(extensionClasses, RPC_DIRECTORY, type.getName(), true);
        // 读取 META-INF/services/
        loadDirectory(extensionClasses, SERVICES_DIRECTORY, type.getName(), true);

        return extensionClasses;
    }


    /**
     * 加载文件夹
     *
     * @param extensionClasses                扩展的class存放的map
     * @param dir                             文件夹路径
     * @param type                            扩展class的type类型
     * @param extensionLoaderClassLoaderFirst 尝试先从ExtensionLoader的ClassLoader加载
     */
    private void loadDirectory(Map<String, Class<? extends T>> extensionClasses, String dir, String type, boolean extensionLoaderClassLoaderFirst) {
        String fileName = dir + type;
        try {
            Enumeration<URL> urls = null;
            ClassLoader classLoader = findClassLoader();

            // try to load from ExtensionLoader's ClassLoader first
            if (extensionLoaderClassLoaderFirst) {
                ClassLoader extensionLoaderClassLoader = ExtensionLoader.class.getClassLoader();
                if (ClassLoader.getSystemClassLoader() != extensionLoaderClassLoader) {
                    urls = extensionLoaderClassLoader.getResources(fileName);
                }
            }

            if (urls == null || !urls.hasMoreElements()) {
                if (classLoader != null) {
                    urls = classLoader.getResources(fileName);
                } else {
                    urls = ClassLoader.getSystemResources(fileName);
                }
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (Throwable t) {
            log.error("Exception occurred when loading extension class (interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    /**
     * 加载 Resource
     *
     * @param extensionClasses 存储class的map集合
     * @param classLoader      classLoader
     * @param resourceURL      resource的URL地址
     */
    private void loadResource(Map<String, Class<? extends T>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) {
        try {
            InputStream inputStream = resourceURL.openStream();
            Properties prop = new Properties();
            prop.load(inputStream);
            prop.forEach((key, value) -> {
                String name = String.valueOf(key);
                String valueStr = String.valueOf(value);
                try {
                    Class<?> clazz = Class.forName(valueStr, true, classLoader);
                    if (!type.isAssignableFrom(clazz)) {
                        throw new IllegalStateException("Error occurred when loading extension class (interface: " +
                                type + ", class line: " + clazz.getName() + "), class "
                                + clazz.getName() + " is not subtype of interface.");
                    }
                    @SuppressWarnings("unchecked") final Class<? extends T> finalClass = (Class<T>) clazz;
                    String[] names = NAME_SEPARATOR.split(name);
                    if (ArrayUtils.isNotEmpty(names)) {
                        for (String n : names) {
                            cacheName(clazz, n);
                            saveInExtensionClass(extensionClasses, finalClass, n);
                        }
                    }

                } catch (Throwable t) {
                    IllegalStateException e = new IllegalStateException("Failed to load extension class (interface: " + type + ", class line: " + valueStr + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                    exceptions.put(valueStr, e);
                }
            });

        } catch (Throwable t) {
            log.error("Exception occurred when loading extension class (interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    /**
     * 报错 扩展class
     *
     * @param extensionClasses 存储扩展class的map
     * @param clazz            clazz
     * @param name             name
     */
    private void saveInExtensionClass(Map<String, Class<? extends T>> extensionClasses, Class<? extends T> clazz, String name) {
        Class<?> c = extensionClasses.get(name);
        if (c == null) {
            extensionClasses.put(name, clazz);
        } else if (c != clazz) {
            String duplicateMsg = "Duplicate extension " + type.getName() + " name " + name + " on " + c.getName() + " and " + clazz.getName();
            log.error(duplicateMsg);
            throw new IllegalStateException(duplicateMsg);
        }
    }

    /**
     * 缓存 name
     *
     * @param clazz class
     * @param name  name
     */
    private void cacheName(Class<?> clazz, String name) {
        if (!cachedNames.containsKey(clazz)) {
            cachedNames.put(clazz, name);
        }
    }

    private void cacheDefaultExtensionName() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation == null) {
            return;
        }

        String value = defaultAnnotation.value();
        if ((value = value.trim()).length() > 0) {
            String[] names = NAME_SEPARATOR.split(value);
            if (names.length > 1) {
                throw new IllegalStateException("More than 1 default extension name on extension " + type.getName()
                        + ": " + Arrays.toString(names));
            }
            if (names.length == 1) {
                cachedDefaultName = names[0];
            }
        }
    }




    // =============== static method ======================

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }


    private static ClassLoader findClassLoader() {
        return ClassUtils.getClassLoader(ExtensionLoader.class);
    }

    /**
     * 读取 ExtensionLoader
     *
     * @param type type类型
     * @return T
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type (" + type +
                    ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
        }

        @SuppressWarnings("unchecked")
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            //noinspection unchecked
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }


    public static void resetExtensionLoader(Class type) {
        ExtensionLoader loader = EXTENSION_LOADERS.get(type);
        if (loader != null) {
            // Remove all instances associated with this loader as well
            Map<String, Class<?>> classes = loader.getExtensionClasses();
            for (Map.Entry<String, Class<?>> entry : classes.entrySet()) {
                EXTENSION_INSTANCES.remove(entry.getValue());
            }
            classes.clear();
            EXTENSION_LOADERS.remove(type);
        }
    }

}
