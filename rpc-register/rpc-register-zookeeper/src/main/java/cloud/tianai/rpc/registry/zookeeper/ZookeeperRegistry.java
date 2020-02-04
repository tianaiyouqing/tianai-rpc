package cloud.tianai.rpc.registry.zookeeper;

import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.registory.api.AbstractRegistry;
import cloud.tianai.rpc.registory.api.NotifyListener;
import cloud.tianai.rpc.registory.api.StatusListener;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/23 12:08
 * @Description: 基于zookeeper的注册器
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    public static final String PROTOCOL = "zookeeper";

    /**
     * 默认的根目录地址.
     */
    private final static String DEFAULT_ROOT = "/tianai-rpc";
    private final static String PATH_SEPARATOR = "/";
    /**
     * 存放自定义监听器和 zk监听器的绑定
     * NotifyListener -> IZkDataListener
     */
    private Map<NotifyListener, IZkChildListener> notifyListenerZkDataListenerMap = new HashMap<>(32);

    /**
     * zk集群时使用，暂时不用.
     */
    public static final String GROUP_KEY = "group";

    /**
     * zookeeper客户端.
     */
    private ZkClient zkClient;
    /**
     * 根目录.
     */
    private String root;
    private Map<URL, Object> registryCache = new ConcurrentHashMap<>(16);

    @Override
    protected void doShutdown() {
        if (zkClient != null) {
            zkClient.close();
        }
        notifyListenerZkDataListenerMap.clear();
    }

    @Override
    protected void doStart(URL url) {
        try {
            init();
        } catch (Exception e) {
            // 启动设置为false
            throw e;
        }
    }

    @Override
    protected void innerRegister(URL url) {
        try {
            try {
                String path = getPath(url);
                create(path + PATH_SEPARATOR + URL.encode(url.toString()));
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } catch (ZkNoNodeException e) {
            // 创建
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected void doSubscribe(StatusListener statusListener) {

    }

    private void init() {
        // todo zookeeperRegistry 这是暂时先设置为单机版
        String address = getRegistryUrl().getAddress();
        int timeout = Integer.parseInt(getRegistryUrl().getParameter("timeout", String.valueOf(5000)));
        zkClient = new ZkClient(address, timeout);
        zkClient.subscribeStateChanges(new WatcherListener(getStatusListenerSet()));
        this.root = getRegistryUrl().getParameter(GROUP_KEY, DEFAULT_ROOT);
        // 判断根节点是否存在，如果不存在则创建， 创建类型必须是持久类型的
        createNodeIfNecessary(this.root, CreateMode.PERSISTENT);
        //todo 启动一个守护线程定时清除为空的持久数据
    }

    private void createNodeIfNecessary(String node, CreateMode createMode) {
        if (zkClient.exists(node)) {
            return;
        }
        zkClient.create(node, null, createMode);
    }


    private String getPath(URL url) {
        return toRootPath() + PATH_SEPARATOR + url.getServiceInterface();
    }

    @Override
    public Result<List<URL>> lookup(URL url) {
        List<URL> urls = Collections.emptyList();
        try {
            List<String> children = zkClient.getChildren(getPath(url));
            if (CollectionUtils.isNotEmpty(children)) {
                urls = new ArrayList<>(children.size());
                for (String child : children) {
                    String decodeChild = URL.decode(child);
                    URL u = URL.valueOf(decodeChild);
                    urls.add(u);
                }
            }
            return Result.ofSuccess(urls);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ofError(e.getMessage());
        }
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        String path = getPath(url);
        // 判断是否存在
        if (!zkClient.exists(path)) {
            // 如果不存在，创建一个空的node
            create(path + PATH_SEPARATOR + URL.encode(url.toString()));
        }
        ZkChildListenerAdapter zkChildListenerAdapter = new ZkChildListenerAdapter(listener);
        zkClient.subscribeChildChanges(path, zkChildListenerAdapter);
        notifyListenerZkDataListenerMap.put(listener, zkChildListenerAdapter);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        IZkChildListener iZkChildListener = notifyListenerZkDataListenerMap.get(listener);
        if (!Objects.isNull(iZkChildListener)) {
            // 如果不为空，则删除该监听器
            zkClient.unsubscribeChildChanges(getPath(url), iZkChildListener);
        }
    }

    public void destroy() {
        if (zkClient != null) {
            try {
                zkClient.close();
                zkClient = null;
            } catch (Exception e) {
                log.error("the service zk close faild info={}", getRegistryUrl());
            }
        }
    }

    private String toRootPath() {
        return root;
    }

    public void create(String path) {
        int indexOf = path.lastIndexOf(PATH_SEPARATOR);
        if (indexOf > 0) {
            // 父节点必须创建持久类型的数据
            createPersistent(path.substring(0, indexOf));
        }
        // 创建临时节点
        createEphemeral(path);
    }

    private void createEphemeral(String path) {
        try {
            zkClient.createEphemeral(path);
        } catch (ZkNodeExistsException e) {
            // 如果node存在， 删除，重新创建
            deleteNode(path);
            createEphemeral(path);
        }
    }

    private void deleteNode(String path) {
        zkClient.delete(path);
    }

    private void createPersistent(String path) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            createPersistent(path.substring(0, i));
        }
        if (!zkClient.exists(path)) {
            try {
                zkClient.createPersistent(path);
            } catch (ZkNodeExistsException e) {
                // 如果node已存在，不做任何处理
                log.warn("ZNode " + path + " already exists.", e);
            }
        }

    }

    private class WatcherListener implements IZkStateListener {

        private Set<StatusListener> statusListenerSet;

        public WatcherListener(Set<StatusListener> statusListenerSet) {
            this.statusListenerSet = statusListenerSet;
        }

        @Override
        public void handleStateChanged(Watcher.Event.KeeperState state) {
            log.info("handleStateChanged() ===> {}", state);
            if (Watcher.Event.KeeperState.Expired == state || Watcher.Event.KeeperState.Disconnected == state) {
                log.info("重连zookeeper ===> ");
                reConnected();
            }
        }

        @Override
        public void handleNewSession() {
            if (log.isInfoEnabled()) {
                log.info("zookeeper new session ========-------->, 重新注册, addressCache=", ZookeeperRegistry.this.registryCache);
            } else {
                System.out.println("new session------------==============================>, addressCache=" + ZookeeperRegistry.this.registryCache);
            }
            // 重新注册
            ZookeeperRegistry.this.reRegister();
        }

        @Override
        public void handleSessionEstablishmentError(Throwable error) {
            // 回话建立错误
            log.warn("zookkper回话建立错误， e=", error);
            reConnected();
        }

        private void reConnected() {
            try {
                System.out.println("执行reConnect");
                ZookeeperRegistry.this.destroy();
                try {
                    //wait for the zk server start success!
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                ZookeeperRegistry.this.init();
            } catch (Exception e) {
                // 报错后重新连接
                e.printStackTrace();
                reConnected();
                return;
            }
            // 重新注册
            ZookeeperRegistry.this.reRegister();
            for (StatusListener statusListener : statusListenerSet) {
                statusListener.reConnected();
            }
        }
    }


    public static class ZkChildListenerAdapter implements IZkChildListener {
        private NotifyListener notifyListener;

        public ZkChildListenerAdapter(NotifyListener notifyListener) {
            this.notifyListener = notifyListener;
        }

        @Override
        public void handleChildChange(String parentPath, List<String> currentChilds) {
            if (CollectionUtils.isEmpty(currentChilds)) {
                notifyListener.notify(Collections.emptyList());
            } else {
                List<URL> urls = new ArrayList<>(currentChilds.size());
                for (String child : currentChilds) {
                    String urlDecode = URL.decode(child);
                    urls.add(URL.valueOf(urlDecode));
                }
                notifyListener.notify(urls);
            }
        }
    }
}
