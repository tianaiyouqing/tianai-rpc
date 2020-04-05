package cloud.tianai.rpc.core.loader;

import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.core.factory.CodecFactory;
import cloud.tianai.rpc.core.factory.RegistryFactory;
import cloud.tianai.rpc.core.factory.RemotingClientFactory;
import cloud.tianai.rpc.core.factory.RemotingServerFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/05 12:19
 * @Description: 初始化扫描配置
 */
@Slf4j
public class RpcPropertiesLoader {

    /**
     * properties路径.
     */
    public static final String PROPERTIES_PATH = "META-INF/dubbo/";
    /**
     * 逗号分隔的正则表达式.
     */
    public static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");
    /**
     * 是否已加载，如果已加载则不会再加载.
     */
    private static AtomicBoolean load = new AtomicBoolean(false);

    @Getter
    private static Map<String, Properties> loadProperties = new ConcurrentHashMap<>();

    public static void loadIfNecessary() {
        if (!load.compareAndSet(false, true)) {
            return;
        }
        // 解析服务注册
        processRegistry();
        // 解析远程server
        processRemotingServer();
        // 解析远程client
        processRemotingClient();
        // 解析序列化
        processCodec();
    }

    private static void processCodec() {
        // 解析 序列化
        processProperties(CodecFactory.class.getName(), 2, (datas) -> {
            datas.forEach((k, v) -> {
                try {
                    CodecFactory.registerCodec(k, v[0], v[1]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 序列化 失败， 未找到该class {}", Arrays.toString(v));
                }
            });
        });
    }

    private static void processRemotingClient() {
        // 解析远程Client
        processProperties(RemotingClientFactory.class.getName(), 1, (datas) -> {
            datas.forEach((k, v) -> {
                try {
                    RemotingClientFactory.addRemotingClient(k, v[0]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 远程Client 失败， 未找到该class {}", Arrays.toString(v));
                }

            });
        });
    }

    private static void processRemotingServer() {
        // 解析远程Server
        processProperties(RemotingServerFactory.class.getName(), 1, (datas) -> {
            datas.forEach((k, v) -> {
                try {
                    RemotingServerFactory.addRemotingServer(k, v[0]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 远程Server 失败， 未找到该class {}", Arrays.toString(v));
                }
            });
        });
    }

    private static void processRegistry() {
        // 解析Registry
        processProperties(RegistryFactory.class.getName(), 1, dataMap -> {
            dataMap.forEach((k, v) -> {
                try {
                    RegistryFactory.addRegistry(k, v[0]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 REGISTRY 失败， 未找到该class {}", Arrays.toString(v));
                }
            });
        });
    }

    private static void processProperties(String name, int splitLen, Consumer<Map<String, String[]>> consumer) {
        Properties properties = loadProperties.computeIfAbsent(name, k -> {
            Properties prop = new Properties();
            return loadProperties(k, prop);
        });

        Map<String, String[]> consumerData = new HashMap<>();
        properties.forEach((k, v) -> {
            String[] split = SPLIT_PATTERN.split(String.valueOf(v));
            if (split.length != splitLen) {
                throw new IllegalStateException("解析 [" + k + "] 失败， 匹配规则错误，" +
                        " data=" + v + ", splitLen=" + splitLen + ", splitFlag=" + SPLIT_PATTERN);
            }
            consumerData.put(String.valueOf(k), split);
        });
        consumer.accept(consumerData);
    }

    private static Properties loadProperties(String name, Properties prop) {
        Enumeration<URL> resources;
        try {
            resources = ClassUtils.getClassLoader().getResources(PROPERTIES_PATH.concat(name));
        } catch (IOException e) {
            // 不做处理
            log.error("加载{}异常e={}", PROPERTIES_PATH, e);
            return prop;
        }

        if (resources != null && resources.hasMoreElements()) {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try {
                    InputStream inputStream = url.openStream();
                    prop.load(inputStream);
                } catch (IOException e) {
                    log.error("加载{}异常e={}", PROPERTIES_PATH, e);
                }

            }
        }
        return prop;
    }

    private static Map<String, Properties> converterProperties(Map<String, List<File>> loadFiles) {
        Map<String, Properties> propertiesMap = new ConcurrentHashMap<>(loadFiles.size());
        loadFiles.forEach((key, files) -> {
            Properties prop = propertiesMap.computeIfAbsent(key, (k) -> new Properties());
            for (File file : files) {
                try {
                    prop.load(new FileInputStream(file));
                } catch (IOException e) {
                    // 不做处理
                    log.error("加载{}异常e={}", PROPERTIES_PATH, e);
                }
            }
        });

        return propertiesMap;
    }

    private static List<URL> readUrls(Enumeration<URL> resources) {
        if (resources != null && resources.hasMoreElements()) {
            List<URL> urls = new LinkedList<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                urls.add(url);
            }
            return urls;
        }
        return Collections.emptyList();
    }


    public static boolean isLoad() {
        return load.get();
    }
}
