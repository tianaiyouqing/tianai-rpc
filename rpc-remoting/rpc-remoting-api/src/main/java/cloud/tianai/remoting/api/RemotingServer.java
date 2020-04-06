package cloud.tianai.remoting.api;

import cloud.tianai.rpc.common.extension.SPI;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 11:34
 * @Description: 远程Server
 */
@SPI
public interface RemotingServer extends RemotingEndpoint{

    /**
     * 远程配置
     * @return
     */
    RemotingServerConfiguration getRemotingServerConfiguration();
}
