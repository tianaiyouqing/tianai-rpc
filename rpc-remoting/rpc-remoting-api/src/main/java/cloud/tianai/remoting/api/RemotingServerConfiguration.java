package cloud.tianai.remoting.api;

import lombok.Data;

import java.util.concurrent.ExecutorService;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/05 11:45
 * @Description: 远程server相关配置
 */
@Data
public class RemotingServerConfiguration extends RemotingConfiguration {

    /** 监听线程数. */
    private Integer bossThreads = 1;

    /** 心跳超时. */
    private Integer idleTimeout = 6000 * 3;
}
