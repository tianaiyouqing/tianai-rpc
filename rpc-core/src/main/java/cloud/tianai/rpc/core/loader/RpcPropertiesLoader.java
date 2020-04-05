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
    private static Map<String, List<File>> loadFiles = new ConcurrentHashMap<>();
    @Getter
    private static Map<String, Properties> loadProperties;

    public static void loadIfNecessary() {
        if (!load.compareAndSet(false, true)) {
            return;
        }
        Enumeration<URL> resources;
        try {
            resources = ClassUtils.getClassLoader().getResources(PROPERTIES_PATH);
        } catch (IOException e) {
            // 不做处理
            log.error("加载{}异常e={}", PROPERTIES_PATH, e);
            return;
        }
        if (resources != null && resources.hasMoreElements()) {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                loadFiles(url);
            }
        }

        // 读取URL
        // 把URL转换成properties
        loadProperties = converterProperties(loadFiles);

        // 解析服务注册
        processRegistry();
        // 解析远程server
        processRemotingServer();
        // 解析远程client
        processRemotingClient();
        // 解析序列化
        processCodec();
    }

    private static void loadFiles(URL resource) {
        String filePath = resource.getFile();
        File dirFile = new File(filePath);
        if (!dirFile.isDirectory()) {
            // 不是文件夹直接返回
            return;
        }
        File[] files = dirFile.listFiles();
        if (files == null) {
            // 如果文件是空，直接返回
            return;
        }
        for (File file : files) {
            String name = file.getName();
            List<File> list = loadFiles.computeIfAbsent(name, (k) -> new LinkedList<File>());
            list.add(file);
        }
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
        Properties properties = loadProperties.get(name);
        if (properties != null) {
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
