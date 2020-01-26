package cloud.tianai.rpc.core.server.constant;

public interface RpcConfigConstant {

    String DEFAULT_CODEC = "hessian2";
    String DEFAULT_REGISTER = "zookeeper";

    String CODEC = "rpc.remoting.codec";
    String TIMEOUT = "rpc.remoting.timeout";
    String REGISTER = "rpc.remoting.register";

    String REGISTRY_HOST = "rpc.remoting.registry.host";
    String REGISTRY_PORT = "rpc.remoting.registry.port";

}
