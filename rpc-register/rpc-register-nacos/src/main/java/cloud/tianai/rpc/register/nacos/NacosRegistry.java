package cloud.tianai.rpc.register.nacos;

import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.threadpool.NamedThreadFactory;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.registory.api.AbstractRegistry;
import cloud.tianai.rpc.registory.api.NotifyListener;
import cloud.tianai.rpc.registory.api.StatusListener;
import cloud.tianai.rpc.registory.api.exception.RpcRegistryException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alibaba.nacos.api.PropertyKeyConst.*;
import static com.alibaba.nacos.client.naming.utils.UtilAndComs.NACOS_NAMING_LOG_NAME;


/**
 * @Author: 天爱有情
 * @Date: 2020/02/04 22:02
 * @Description: Nacos注册中心
 */
@Slf4j
public class NacosRegistry extends AbstractRegistry {

    public static final String PROTOCOL = "nacos";
    public static final String NACOS_UP_STATUS = "UP";
    public static final String NACOS_DOWN_STATUS = "DOWN";
    public static final String URL_KEY = "url";
    public static final String DEFAULT_GROUP_NAME = "DEFAULT_GROUP";
    private String groupName = DEFAULT_GROUP_NAME;

    ScheduledExecutorService scheduledExecutorService;


    private Map<NotifyListener, EventListenerAdapter> listenerAdapterMap = new ConcurrentHashMap<>(16);

    private AtomicBoolean statusThread = new AtomicBoolean(false);
    private NamingService namingService;

    private Instance createInstance(URL url) {
        Instance instance = new Instance();
        instance.setIp(url.getHost());
        instance.setPort(url.getPort());
        instance.setMetadata(Collections.singletonMap(URL_KEY, url.toFullString()));
        return instance;
    }

    @Override
    public Result<List<URL>> lookup(URL url) {
        List<URL> res = new LinkedList<>();
        String path = getPath(url);
        execute(ns -> {
            List<Instance> instances = ns.getAllInstances(path);
            res.addAll(converterUrl(instances));
        });
        return Result.ofSuccess(res);
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }


    public String getPath(URL url) {
        return url.getServiceInterface();
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        EventListenerAdapter eventListenerAdapter = new EventListenerAdapter(listener);
        execute(na -> na.subscribe(getPath(url), groupName, eventListenerAdapter));
        listenerAdapterMap.put(listener, eventListenerAdapter);
    }


    public List<URL> converterUrl(List<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyList();
        }

        List<URL> urls = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            String urlStr = instance.getMetadata().get(URL_KEY);
            if (StringUtils.isNotBlank(urlStr)) {
                urls.add(URL.valueOf(urlStr));
            }
        }
        return urls;
    }

    private void startStatusListenerThreadIfNecessary() {
        if (isStart() && statusThread.compareAndSet(false, true)) {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                execute(na -> {
                    if (NACOS_DOWN_STATUS.equals(na.getServerStatus())) {
                        // 如果状态为STOP状态
                        try {
                            connect(getRegistryUrl(), 0, getRetryCount());
                        } catch (ConnectException e) {
                            destroy();
                            throw new RpcException("TIANAI-RPC REGISTRY NACOS 重连失败. address:".concat(getRegistryUrl().getAddress()));
                        }
                        // 重连成功
                        if (log.isInfoEnabled()) {
                            log.info("TIANAI-RPC REGISTRY NACOS 重连成功， address：" + getRegistryUrl().getAddress());
                        } else {
                            System.out.println("TIANAI-RPC REGISTRY NACOS 重连成功， address：" + getRegistryUrl().getAddress());
                        }
                        // 重新注册
                        reRegister();
                        // 通知状态监听器，重连成功
                        for (StatusListener statusListener : getStatusListenerSet()) {
                            statusListener.reConnected();
                        }
                    }
                });
            }, 2, 2, TimeUnit.SECONDS);
        }
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        EventListenerAdapter eventListenerAdapter = listenerAdapterMap.get(listener);
        if (eventListenerAdapter != null) {
            execute(na -> na.unsubscribe(getPath(url), groupName, eventListenerAdapter));
        }
    }


    private ScheduledExecutorService createScheduledExecutorService(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory("nacos-status-listener-thread"));
    }


    public NamingService connect(URL url, Integer currRetry, Integer retry) throws ConnectException {
        Properties nacosProperties = getNacosProperties(url);
        NamingService ns;
        try {
            ns = NacosFactory.createNamingService(nacosProperties);
        } catch (NacosException e) {
            throw new RpcException(e.getMessage(), e);
        }
        if (NACOS_DOWN_STATUS.equals(ns.getServerStatus())) {
            // 如果未启动
            if (currRetry >= retry) {
                throw new ConnectException("Nacos 连接失败");
            }
            if (log.isErrorEnabled()) {
                log.error("TIANAI-RPC REGISTRY NACOS 连接失败...");
            } else {
                System.out.println("TIANAI-RPC REGISTRY NACOS 连接失败...");
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100 * (currRetry + 1));
            } catch (InterruptedException e) {
                // 不做处理
            }
            return connect(url, ++currRetry, retry);
        }
        return ns;
    }

    private Properties getNacosProperties(URL url) {
        Properties properties = new Properties();
        properties.put(SERVER_ADDR, url.getAddress());
        putPropertyIfAbsent(url, properties, NACOS_NAMING_LOG_NAME);
        putPropertyIfAbsent(url, properties, IS_USE_CLOUD_NAMESPACE_PARSING);
        putPropertyIfAbsent(url, properties, IS_USE_ENDPOINT_PARSING_RULE);
        putPropertyIfAbsent(url, properties, ENDPOINT);
        putPropertyIfAbsent(url, properties, ENDPOINT_PORT);
        putPropertyIfAbsent(url, properties, NAMESPACE);
        putPropertyIfAbsent(url, properties, ACCESS_KEY);
        putPropertyIfAbsent(url, properties, SECRET_KEY);
        putPropertyIfAbsent(url, properties, RAM_ROLE_NAME);
        putPropertyIfAbsent(url, properties, CONTEXT_PATH);
        putPropertyIfAbsent(url, properties, CLUSTER_NAME);
        putPropertyIfAbsent(url, properties, ENCODE);
        putPropertyIfAbsent(url, properties, CONFIG_LONG_POLL_TIMEOUT);
        putPropertyIfAbsent(url, properties, CONFIG_RETRY_TIME);
        putPropertyIfAbsent(url, properties, MAX_RETRY);
        putPropertyIfAbsent(url, properties, ENABLE_REMOTE_SYNC_CONFIG);
        putPropertyIfAbsent(url, properties, NAMING_LOAD_CACHE_AT_START, "true");
        putPropertyIfAbsent(url, properties, NAMING_CLIENT_BEAT_THREAD_COUNT);
        putPropertyIfAbsent(url, properties, NAMING_POLLING_THREAD_COUNT);
        return properties;
    }

    private static void putPropertyIfAbsent(URL url, Properties properties, String propertyName) {
        String propertyValue = url.getParameter(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            properties.setProperty(propertyName, propertyValue);
        }
    }

    private static void putPropertyIfAbsent(URL url, Properties properties, String propertyName, String defaultValue) {
        String propertyValue = url.getParameter(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            properties.setProperty(propertyName, propertyValue);
        } else {
            properties.setProperty(propertyName, defaultValue);
        }
    }

    @Override
    protected void doDestroy() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        namingService = null;
        listenerAdapterMap.clear();
    }

    @Override
    protected void doStart(URL url) {
        try {
            namingService = connect(url, 0, getRetryCount());
            // 创建调度线程
            scheduledExecutorService = createScheduledExecutorService(1);
            // 开启状态监听线程
            startStatusListenerThreadIfNecessary();
        } catch (Exception e) {
            destroy();
            // 连接失败，抛出异常
            throw new RpcRegistryException(e.getMessage(), e);
        }
    }

    @Override
    protected void innerRegister(URL url) {
        String path = getPath(url);
        Instance instance = createInstance(url);
        execute(ns -> ns.registerInstance(path, instance));
    }

    @Override
    protected void doSubscribe(StatusListener statusListener) {
        startStatusListenerThreadIfNecessary();
    }

    private void execute(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getErrMsg(), e);
            }
        }
    }

    @Override
    public boolean isActive() {
        return NACOS_UP_STATUS.equals(namingService.getServerStatus());
    }

    interface NamingServiceCallback {

        /**
         * Callback
         *
         * @param namingService {@link NamingService}
         * @throws NacosException
         */
        void callback(NamingService namingService) throws NacosException;
    }


    public class EventListenerAdapter implements EventListener {
        private NotifyListener notifyListener;

        public EventListenerAdapter(NotifyListener notifyListener) {
            this.notifyListener = notifyListener;
        }

        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;
                List<Instance> instances = namingEvent.getInstances();
                List<URL> urls = NacosRegistry.this.converterUrl(instances);
                notifyListener.notify(urls);
            }
        }
    }
}
