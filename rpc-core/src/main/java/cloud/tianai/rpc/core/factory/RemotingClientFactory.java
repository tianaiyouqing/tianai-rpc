package cloud.tianai.rpc.core.factory;

import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/27 15:15
 * @Description: 远程客户端工厂
 */
public class RemotingClientFactory {
    private static Map<String, Class<? extends RemotingClient>> remotingServerMap = new HashMap<>(2);

    static {
        try {
            // 注册netty客户端
            addRemotingServer("netty", "cloud.tianai.remoting.netty.NettyClient");
        } catch (ClassNotFoundException e) {
            // 不做处理
        }
    }

    public static RemotingClient create(String protocol) {
        Class<? extends RemotingClient> remotingClientClazz = remotingServerMap.get(protocol);
        if (Objects.isNull(remotingClientClazz)) {
            // 没有对应类型，直接返回空
            return null;
        }
        return create(remotingClientClazz);
    }

    public static RemotingClient create(Class<? extends RemotingClient> clientClazz) {

        try {
            RemotingClient remotingClient = ClassUtils.createObject(clientClazz);
            return remotingClient;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    public static void addRemotingServer(String protocol, Class<? extends RemotingClient> clientClass) {
        remotingServerMap.remove(protocol);
        remotingServerMap.put(protocol, clientClass);
    }

    public static void addRemotingServer(String protocol, String clientClassStr) throws ClassNotFoundException {
        Class<?> clazz = ClassUtils.forName(clientClassStr);
        if (!RemotingClient.class.isAssignableFrom(clazz) || clazz.isInterface()) {
            // 如果不是 RemotingServer 的子类，或者是个接口，则直接报错
            throw new RpcException("该class不是 RemotingServer 的子类， 或者 是个接口");
        }

        //noinspection unchecked
        addRemotingServer(protocol, (Class<? extends RemotingClient>) clazz);
    }
}
