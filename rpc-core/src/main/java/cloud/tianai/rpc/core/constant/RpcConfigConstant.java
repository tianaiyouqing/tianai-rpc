package cloud.tianai.rpc.core.constant;

public interface RpcConfigConstant {
    /** 默认的工作线程数. */
    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
    String DEFAULT_CODEC = "hessian2";
    String DEFAULT_REGISTER = "zookeeper";
    String DEFAULT_PROTOCOL = "netty";


    String PROTOCOL = "rpc.remoting.protocol";
    String TIMEOUT = "rpc.remoting.timeout";
    String HOST = "rpc.remoting.host";
    String PORT = "rpc.remoting.port";
    String WORKER_THREADS = "rpc.remoting.worker.threads";

    String CODEC = "rpc.remoting.codec";


    String REGISTER = "rpc.remoting.register";
    String REGISTRY_HOST = "rpc.remoting.registry.host";
    String REGISTRY_PORT = "rpc.remoting.registry.port";

}
