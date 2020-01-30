package cloud.tianai.remoting.api;

import cloud.tianai.remoting.api.exception.RpcRemotingException;

import java.net.SocketAddress;

public interface RemotingClient extends RemotingEndpoint {

    void doConnect();

    SocketAddress getRemoteAddress();
}
