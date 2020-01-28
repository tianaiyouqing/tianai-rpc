package cloud.tianai.rpc.core.factory;

import cloud.tianai.remoting.api.RemotingServer;
import cloud.tianai.remoting.api.RemotingServerConfiguration;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/25 17:37
 * @Description: 远程 server创建工厂
 */
public class RemotingServerFactory {

    private static Map<String, Class<? extends RemotingServer>> remotingServerMap = new HashMap<>(2);

    static {
        try {
            // 注册netty服务器
            addRemotingServer("netty", "cloud.tianai.remoting.netty.NettyServer");
        } catch (ClassNotFoundException e) {
            // 不做处理
        }
    }

    public static RemotingServer create(String protocol) {
        Class<? extends RemotingServer> remotingServerClazz = remotingServerMap.get(protocol);
        if (Objects.isNull(remotingServerClazz)) {
            // 没有对应类型，直接返回空
            return null;
        }
        return create(remotingServerClazz);
    }

    public static RemotingServer create(Class<? extends RemotingServer> serverClazz) {

        try {
            RemotingServer remotingServer = ClassUtils.createObject(serverClazz);
            return remotingServer;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    public static void addRemotingServer(String protocol, Class<? extends RemotingServer> serverClass) {
        remotingServerMap.remove(protocol);
        remotingServerMap.put(protocol, serverClass);
    }

    public static void addRemotingServer(String protocol, String serverClassStr) throws ClassNotFoundException {
        Class<?> clazz = ClassUtils.forName(serverClassStr);
        if (!RemotingServer.class.isAssignableFrom(clazz) || clazz.isInterface()) {
            // 如果不是 RemotingServer 的子类，或者是个接口，则直接报错
            throw new RpcException("该class不是 RemotingServer 的子类， 或者 是个接口");
        }

        //noinspection unchecked
        addRemotingServer(protocol, (Class<? extends RemotingServer>) clazz);
    }
}
