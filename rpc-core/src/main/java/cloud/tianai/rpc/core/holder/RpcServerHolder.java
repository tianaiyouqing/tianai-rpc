package cloud.tianai.rpc.core.holder;

import cloud.tianai.remoting.api.RemotingServer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/28 18:09
 * @Description: RPCServer持有者
 */
public class RpcServerHolder {
    private static final String SPLIT = ":";

    /**
     * (protocol:address) -> RpcClient.
     */
    private static Map<String, RemotingServer> rpcServerMap = new ConcurrentHashMap<>(32);
    private static Map<String, Lock> lockMap = new ConcurrentHashMap<>(32);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(RpcServerHolder :: shutdown));
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
    public static RemotingServer getRpcServer(String protocol, String address) {
        return rpcServerMap.get(getKey(protocol, address));
    }

    private static String getKey(String protocol, String address) {
        return protocol.concat(SPLIT).concat(address);
    }

    public static RemotingServer computeIfAbsent(String protocol, String address, BiFunction<String, String, RemotingServer> supplier) {
        RemotingServer rpcServer = getRpcServer(protocol, address);
        if (rpcServer == null) {
            Lock lock = getLock(protocol, address);
            synchronized (lock) {
                if ((rpcServer = getRpcServer(protocol, address)) == null) {
                    rpcServer = supplier.apply(protocol, address);
                    putRpcServer(protocol, address, rpcServer);
                }
            }
        }
        return rpcServer;
    }


    /**
     * 添加RpcClient
     *
     * @param protocol
     * @param address
     * @param rpcServer
     */
    public static void putRpcServer(String protocol, String address, RemotingServer rpcServer) {
        String key = getKey(protocol, address);
        RemotingServer oldRpcServer = rpcServerMap.remove(key);
        rpcServerMap.put(key, rpcServer);

        if (oldRpcServer != null) {
            oldRpcServer.stop();
        }
    }

    /**
     * 删除RpcClient
     *
     * @param protocol
     * @param address
     */
    public static void removeRpcServer(String protocol, String address) {
        String key = getKey(protocol, address);
        RemotingServer rpcServer = rpcServerMap.remove(key);
        if (rpcServer != null) {
            rpcServer.stop();
        }
    }

    public static void removeRpcServer(String key) {
        RemotingServer rpcServer = rpcServerMap.remove(key);
        if (rpcServer != null) {
            rpcServer.stop();
        }
    }

    public static void shutdown() {
        for (String key : rpcServerMap.keySet()) {
            removeRpcServer(key);
        }
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
