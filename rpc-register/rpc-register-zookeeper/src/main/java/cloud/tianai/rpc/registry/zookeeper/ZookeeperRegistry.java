package cloud.tianai.rpc.registry.zookeeper;

import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.registory.api.NotifyListener;
import cloud.tianai.rpc.registory.api.Registry;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/23 12:08
 * @Description: 基于zookeeper的注册器
 */
@Slf4j
public class ZookeeperRegistry implements Registry {

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
     * 创建zookeeper时需要的url地址.
     */
    private URL zookeeperUrl;

    /**
     * zookeeper客户端.
     */
    private ZkClient zkClient;
    /**
     * 根目录.
     */
    private String root;

    private AtomicBoolean start = new AtomicBoolean(false);

    @Override
    public Registry start(URL url) {
        if (!start.compareAndSet(false, true)) {
            throw new RpcException("已经启动，不可重复启动");
        }
        this.zookeeperUrl = url;
        init();
        return this;
    }

    @Override
    public void shutdown() {
        if(zkClient != null) {
            zkClient.close();
        }
        notifyListenerZkDataListenerMap.clear();
        start.set(false);
    }

    private void init() {
        // todo zookeeperRegistry 这是暂时先设置为单机版
        String address = zookeeperUrl.getAddress();
        int timeout = Integer.parseInt(zookeeperUrl.getParameter("timeout", String.valueOf(5000)));
        zkClient = new ZkClient(address, timeout);
        zkClient.subscribeStateChanges(new WatcherListener());
        this.root = zookeeperUrl.getParameter(GROUP_KEY, DEFAULT_ROOT);
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


    @Override
    public Result<?> register(URL url) {
        try {
            try {
                String path = getPath(url);
                create(path + PATH_SEPARATOR + URL.encode(url.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ZkNoNodeException e) {
            // 创建
        }
        return Result.ofSuccess("success");
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
                log.error("the service zk close faild info={}", zookeeperUrl);
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
        if (!zkClient.exists(path)) {
            zkClient.createEphemeral(path);
        }
    }

    private void createPersistent(String path) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            createPersistent(path.substring(0, i));
        }
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path);
        }
    }

    private class WatcherListener implements IZkStateListener {

        @Override
        public void handleStateChanged(Watcher.Event.KeeperState state) {
            if (Watcher.Event.KeeperState.Expired == state || Watcher.Event.KeeperState.Disconnected == state) {
                System.out.println("重连zookeeper");
                reConnected();
            }
        }

        @Override
        public void handleNewSession() {

        }

        @Override
        public void handleSessionEstablishmentError(Throwable error) {

        }

        private void reConnected() {
            ZookeeperRegistry.this.destroy();
            try {
                //wait for the zk server start success!
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            ZookeeperRegistry.this.init();
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
