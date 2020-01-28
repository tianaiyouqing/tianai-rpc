package cloud.tianai.rpc.core.constant;

public interface RpcClientConfigConstant extends RpcConfigConstant {

    Integer DEFAULT_REQUEST_TIMEOUT = 5000;

    String REQUEST_TIMEOUT = "rpc.remoting.request.timeout";

    String LOAD_BALANCE = "rpc.remoting.loadbanance";
}
