package cloud.tianai.rpc.core.loadbalance.impl;

import cloud.tianai.rpc.remoting.api.RemotingChannelHolder;
import cloud.tianai.rpc.remoting.api.RemotingClient;
import cloud.tianai.rpc.remoting.api.RemotingConfiguration;
import cloud.tianai.rpc.remoting.api.exception.RpcRemotingException;

import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

public class RemotingClientTestImpl implements RemotingClient {

    private int weight = 100;
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

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int getWeight() {
        return weight;
    }
}
