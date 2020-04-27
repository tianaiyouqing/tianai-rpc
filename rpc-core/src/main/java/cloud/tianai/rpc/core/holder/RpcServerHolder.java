package cloud.tianai.rpc.core.holder;

import cloud.tianai.rpc.remoting.api.RemotingServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

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

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(RpcServerHolder::shutdown));
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
        String key = getKey(protocol, address);
        RemotingServer rpcServer = rpcServerMap.computeIfAbsent(key, (k) -> supplier.apply(protocol, address));
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
            oldRpcServer.destroy();
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
            rpcServer.destroy();
        }
    }

    public static void removeRpcServer(String key) {
        RemotingServer rpcServer = rpcServerMap.remove(key);
        if (rpcServer != null) {
            rpcServer.destroy();
        }
    }

    public static void shutdown() {
        for (String key : rpcServerMap.keySet()) {
            removeRpcServer(key);
        }
    }
}
