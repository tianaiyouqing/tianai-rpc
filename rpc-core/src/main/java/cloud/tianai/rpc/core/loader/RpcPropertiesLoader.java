package cloud.tianai.rpc.core.loader;

import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.factory.CodecFactory;
import cloud.tianai.rpc.core.factory.RegistryFactory;
import cloud.tianai.rpc.core.factory.RemotingClientFactory;
import cloud.tianai.rpc.core.factory.RemotingServerFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/05 12:19
 * @Description: 初始化扫描配置
 */
@Slf4j
public class RpcPropertiesLoader {
    public static final String PROPERTIES_PATH = "META-INF/tianai-rpc.properties";
    public static final String SPLIT = ",";

    private static AtomicBoolean load = new AtomicBoolean(false);
    @Getter
    private static List<URL> loadUrls;
    @Getter
    private static List<Properties> loadProperties;

    public static void loadIfNecessary() {
        if (!load.compareAndSet(false, true)) {
            return;
        }
        Enumeration<URL> resources = null;
        try {
            resources = ClassUtils.getClassLoader().getResources(PROPERTIES_PATH);
        } catch (IOException e) {
            // 不做处理
            log.error("加载{}}异常e={}", PROPERTIES_PATH, e);
            return;
        }
        // 读取URL
        loadUrls = readUrls(resources);
        // 把URL转换成properties
        loadProperties = converterProperties(loadUrls);

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
        processProperties(CodecFactory.class.getName(), 3, (datas) -> {
            for (String[] data : datas) {
                try {
                    CodecFactory.registerCodec(data[0], data[1], data[2]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 序列化 失败， 未找到该class {}", Arrays.toString(data));
                }
            }
        });
    }

    private static void processRemotingClient() {
        // 解析远程Client
        processProperties(RemotingClientFactory.class.getName(), 2, (datas) -> {
            for (String[] data : datas) {
                try {
                    RemotingClientFactory.addRemotingClient(data[0], data[1]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 远程Client 失败， 未找到该class {}", Arrays.toString(data));
                }
            }
        });
    }

    private static void processRemotingServer() {
        // 解析远程Server
        processProperties(RemotingServerFactory.class.getName(), 2, (datas) -> {
            for (String[] data : datas) {
                try {
                    RemotingServerFactory.addRemotingServer(data[0], data[1]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 远程Server 失败， 未找到该class {}", Arrays.toString(data));
                }
            }
        });
    }

    private static void processRegistry() {
        // 解析Registry
        processProperties(RegistryFactory.class.getName(), 2, (datas) -> {
            for (String[] data : datas) {
                try {
                    RegistryFactory.addRegistry(data[0], data[1]);
                } catch (ClassNotFoundException e) {
                    // 不做处理
                    log.error("加载 REGISTRY 失败， 未找到该class {}", Arrays.toString(data));
                }
            }
        });
    }

    private static void processProperties(String name, int splitLen, Consumer<List<String[]>> consumer) {
        List<String> propData = new LinkedList<>();
        for (Properties property : loadProperties) {
            String p = property.getProperty(name);
            if (StringUtils.isNotBlank(p)) {
                propData.add(p);
            }
        }
        if (CollectionUtils.isNotEmpty(propData)) {
            // 不为空才调用consumer方法
            List<String[]> res = new ArrayList<>(propData.size());
            for (String propDatum : propData) {
                String[] split = propDatum.split(SPLIT);
                if (split.length != splitLen) {
                    throw new IllegalStateException("解析 [tianai-rpc.properties] 失败， 匹配规则错误，" +
                            " data=" + propDatum + ", splitLen=" + splitLen + ", splitFlag=" + SPLIT);
                }
                // 去一下空格
                for (int i = 0; i < split.length; i++) {
                    split[i] = split[i].trim();
                }
                res.add(split);
            }
            consumer.accept(res);
        }
    }

    private static List<Properties> converterProperties(List<URL> readUrls) {
        List<Properties> properties = new ArrayList<>(readUrls.size());
        for (URL url : readUrls) {
            Properties prop = new Properties();
            try {
                prop.load(url.openStream());
            } catch (IOException e) {
                // 不做处理
                log.error("读取tianai-rpc.properties失败， path={}, e={}", url.getPath(), e);
                continue;
            }
            properties.add(prop);
        }
        return properties;
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
