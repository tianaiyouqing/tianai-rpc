package cloud.tianai.rpc.core.server.constant;

public interface RpcServerConfigConstant extends RpcConfigConstant{

    String DEFAULT_PROTOCOL = "netty";

    String HOST = "rpc.remoting.server.host";
    String PORT = "rpc.remoting.server.port";
    String PROTOCOL = "rpc.remoting.server.protocol";
    String WORKER_THREADS = "rpc.remoting.server.worker.threads";
    String BOSS_THREADS = "rpc.remoting.server.boss.threads";
}
