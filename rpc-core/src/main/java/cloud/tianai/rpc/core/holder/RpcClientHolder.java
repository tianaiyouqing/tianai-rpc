package cloud.tianai.rpc.core.holder;

import cloud.tianai.rpc.remoting.api.RemotingClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

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

    static {
        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(RpcClientHolder::shutdown));
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
        String key = getKey(protocol, address);
        RemotingClient rpcClient = rpcClientMap.computeIfAbsent(key, (k) -> supplier.apply(protocol, address));
        return rpcClient;
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
}
