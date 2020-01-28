package cloud.tianai.rpc.core.holder;

import cloud.tianai.rpc.core.client.RpcClient;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
    private static Map<String, RpcClient> rpcClientMap = new ConcurrentHashMap<>(32);
    private static Map<String, Lock> lockMap = new ConcurrentHashMap<>(32);

    public static Lock getLock(String protocol, String address) {
        Lock lock = lockMap.computeIfAbsent(protocol + SPLIT + address, (k) -> {
            String[] split = k.split(SPLIT);
            Lock l = new Lock(split[0], split[1]);
            return l;
        });
        return lock;
    }

    /**
     * 获取RpcClient如果存在
     * @param protocol
     * @param address
     * @return
     */
    public static RpcClient getRpcClient(String protocol, String address) {
        return rpcClientMap.get(protocol + SPLIT + address);
    }

    /**
     * 添加RpcClient
     * @param protocol
     * @param address
     * @param rpcClient
     */
    public static void putRpcClient(String protocol, String address, RpcClient rpcClient) {
        String flag = protocol + SPLIT + address;
        rpcClientMap.remove(flag);
        rpcClientMap.put(flag, rpcClient);
    }

    /**
     * 删除RpcClient
     * @param protocol
     * @param address
     */
    public static void removeRpcClient(String protocol, String address) {
        String flag = protocol + SPLIT + address;
        rpcClientMap.remove(flag);
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
