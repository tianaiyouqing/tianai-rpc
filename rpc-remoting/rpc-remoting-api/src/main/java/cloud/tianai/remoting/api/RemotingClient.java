package cloud.tianai.remoting.api;

import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

public interface RemotingClient extends RemotingEndpoint {

    void doConnect();

    void reconnect(int retryCount) throws TimeoutException;

    SocketAddress getRemoteAddress();
}
