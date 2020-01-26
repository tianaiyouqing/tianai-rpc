package cloud.tianai.rpc.registry.zookeeper;

import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.registory.api.NotifyListener;
import cloud.tianai.rpc.registory.api.Registry;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/23 12:08
 * @Description: 基于zookeeper的注册器
 */
public class ZookeeperRegistry implements Registry {

    /** 默认的根目录地址. */
    private final static String DEFAULT_ROOT = "/tianai-rpc";

    /**
     * 存放自定义监听器和 zk监听器的绑定
     * NotifyListener -> IZkDataListener
     */
    private Map<NotifyListener, IZkDataListener> notifyListenerIZkDataListenerMap = new HashMap<>(32);

    /** zk集群时使用，暂时不用. */
    String GROUP_KEY = "group";

    /** 创建zookeeper时需要的url地址. */
    private URL zookeeperUrl;

    /** zookeeper客户端. */
    private ZkClient zkClient;
    /** 根目录. */
    private String root;

    private AtomicBoolean start = new AtomicBoolean(false);

    private ReentrantLock lock = new ReentrantLock();

    @Override
    public Registry start(URL url) {
        if(!start.compareAndSet(false, true)) {
                throw new RpcException("已经启动，不可重复启动");
        }
        this.zookeeperUrl = url;
        init();
        return this;
    }

    private void init() {
        // todo zookeeperRegistry 这是暂时先设置为单机版
        String address = zookeeperUrl.getAddress();
        zkClient = new ZkClient(address);
        this.root = zookeeperUrl.getParameter(GROUP_KEY, DEFAULT_ROOT);
        // 判断根节点是否存在，如果不存在则创建， 创建类型必须是持久类型的
        createNodeIfNecessary(this.root, CreateMode.PERSISTENT);
    }

    private void createNodeIfNecessary(String node, CreateMode createMode) {
        if (zkClient.exists(node)) {
            return;
        }
        zkClient.create(node, null, createMode);
    }


    @Override
    public Result<?> register(URL url) {
        String urlPath = url.toString();
        String address = getInterfacePath(url.getServiceInterface());
        List<URL> urls;
        try {
            urls = new ArrayList<>(1);
            urls.add(url);
            String res = zkClient.create(address, urls, CreateMode.EPHEMERAL);
        } catch (ZkNodeExistsException e) {
            // 表示该节点已存在, 把当前url添加到该列表
            urls = addUrl(address, url);
        } catch (Exception e) {
            return Result.ofError(e.getMessage());
        }
        return Result.ofSuccess(urls);
    }

    private String getInterfacePath(String serviceInterface) {
        String path = root + "/" + serviceInterface;
        return path;
    }

    private List<URL> addUrl(String address, URL url) {
        // 创建失败
        List<URL> urls = zkClient.readData(address);
        if (CollectionUtils.isEmpty(urls)) {
            urls = new ArrayList<>(1);
        }
        urls.add(url);
        // 回写
        zkClient.writeData(address, urls);
        return urls;
    }

    @Override
    public Result<List<URL>> lookup(URL url) {
        String path = getInterfacePath(url.getServiceInterface());
        try {
            List<URL> urls = zkClient.readData(path);
            if (null == urls) {
                return Result.ofSuccess(Collections.emptyList());
            }
            return Result.ofSuccess(urls);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ofError(e.getMessage());
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        String path = getInterfacePath(url.getServiceInterface());
        // 判断是否存在
        if (!zkClient.exists(path)) {
            // 如果不存在，创建一个空的node
            createNodeIfNecessary(path, CreateMode.EPHEMERAL);
        }
        ZkDataListenerAdapter zkDataListenerAdapter = new ZkDataListenerAdapter(listener);
        zkClient.subscribeDataChanges(path, zkDataListenerAdapter);
        notifyListenerIZkDataListenerMap.put(listener, zkDataListenerAdapter);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        IZkDataListener iZkDataListener = notifyListenerIZkDataListenerMap.get(listener);
        if(!Objects.isNull(iZkDataListener)) {
            // 如果不为空，则删除该监听器
            zkClient.unsubscribeDataChanges(getInterfacePath(url.getServiceInterface()), iZkDataListener);
        }
    }




    public static class ZkDataListenerAdapter implements IZkDataListener {
        private NotifyListener notifyListener;

        public ZkDataListenerAdapter(NotifyListener notifyListener) {
            this.notifyListener = notifyListener;
        }

        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {
            notifyListener.notify((List<URL>) data);
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            notifyListener.notify(Collections.emptyList());
        }
    }
}
