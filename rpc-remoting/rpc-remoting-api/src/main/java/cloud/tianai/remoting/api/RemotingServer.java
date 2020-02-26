package cloud.tianai.remoting.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 11:34
 * @Description: 远程Server
 */
public interface RemotingServer extends RemotingEndpoint{

    /**
     * 远程配置
     * @return
     */
    RemotingServerConfiguration getRemotingServerConfiguration();
}
