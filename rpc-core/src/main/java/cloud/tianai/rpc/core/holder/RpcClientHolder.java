package cloud.tianai.rpc.core.holder;

import cloud.tianai.remoting.api.RemotingClient;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 11:34
 * @Description: RPC客户端持有者
 */
public class RpcClientHolder {

    private static final String SPLIT = ":";

    /**
     * (protocol:address) -> RpcClient.
     */
    private static Map<String, RemotingClient> rpcClientMap = new ConcurrentHashMap<>(32);
    private static Map<String, Lock> lockMap = new ConcurrentHashMap<>(32);

    static {
        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(RpcClientHolder::shutdown));
    }


    public static Lock getLock(String protocol, String address) {
        Lock lock = lockMap.computeIfAbsent(getKey(protocol, address), (k) -> {
            String[] split = k.split(SPLIT);
            Lock l = new Lock(split[0], split[1]);
            return l;
        });
        return lock;
    }

    /**
     * 获取RpcClient如果存在
     *
     * @param protocol
     * @param address
     * @return
     */
    public static RemotingClient getRpcClient(String protocol, String address) {
        return rpcClientMap.get(getKey(protocol, address));
    }

    public static RemotingClient computeIfAbsent(String protocol, String address, BiFunction<String, String, RemotingClient> supplier) {
        RemotingClient rpcClient = getRpcClient(protocol, address);
        if (rpcClient == null) {
            Lock lock = getLock(protocol, address);
            synchronized (lock) {
                if ((rpcClient = getRpcClient(protocol, address)) == null) {
                    rpcClient = supplier.apply(protocol, address);
                    putRpcClient(protocol, address, rpcClient);
                }
            }
        }
        return rpcClient;
    }


    /**
     * 添加RpcClient
     *
     * @param protocol
     * @param address
     * @param rpcClient
     */
    public static void putRpcClient(String protocol, String address, RemotingClient rpcClient) {
        String key = getKey(protocol, address);
        RemotingClient oldRpcClient = rpcClientMap.remove(key);
        rpcClientMap.put(key, rpcClient);
        if (oldRpcClient != null) {
            oldRpcClient.stop();
        }
    }

    public static void removeRpcClient(String key) {
        RemotingClient rpcClient = rpcClientMap.remove(key);
        if (rpcClient != null) {
            rpcClient.stop();
        }
    }

    public static String getKey(String protocol, String address) {
        return protocol.concat(SPLIT).concat(address);
    }

    public static void shutdown() {
        for (String key : rpcClientMap.keySet()) {
            removeRpcClient(key);
        }
    }

    /**
     * 删除RpcClient
     *
     * @param protocol
     * @param address
     */
    public static void removeRpcClient(String protocol, String address) {
        String key = getKey(protocol, address);
        removeRpcClient(key);
    }

    public static class Lock {
        private String protocol;
        private String address;

        public Lock(String protocol, String address) {
            this.protocol = protocol;
            this.address = address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Lock lock = (Lock) o;
            return Objects.equals(protocol, lock.protocol) &&
                    Objects.equals(address, lock.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(protocol, address);
        }

        @Override
        public String toString() {
            return "Lock{" +
                    "protocol='" + protocol + '\'' +
                    ", address='" + address + '\'' +
                    '}';
        }
    }

}
