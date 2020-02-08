package cloud.tianai.rpc.core.loadbalance.impl;

import cloud.tianai.remoting.api.RemotingChannelHolder;
import cloud.tianai.remoting.api.RemotingClient;
import cloud.tianai.remoting.api.RemotingConfiguration;
import cloud.tianai.remoting.api.exception.RpcRemotingException;
import cloud.tianai.rpc.registory.api.Registry;

import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

public class LoadBalancedRemotingClientProxy implements RemotingClient {

    private final Registry registry;

    public LoadBalancedRemotingClientProxy(Registry registry) {
        assert registry != null;
        this.registry = registry;
    }

    @Override
    public void doConnect() {

    }

    @Override
    public void reconnect(int retryCount) throws TimeoutException {

    }

    @Override
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public RemotingChannelHolder start(RemotingConfiguration config) throws RpcRemotingException {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public RemotingChannelHolder getChannel() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStart() {
        return false;
    }

    @Override
    public String getRemotingType() {
        return null;
    }
}
