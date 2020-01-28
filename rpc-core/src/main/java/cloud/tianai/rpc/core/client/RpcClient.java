package cloud.tianai.rpc.core.client;

import cloud.tianai.remoting.api.Request;
import cloud.tianai.remoting.api.Response;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;

import java.util.Properties;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/27 15:01
 * @Description: rpc 客户端
 */
public interface RpcClient {

    /**
     * 启动客户端
     * @param url
     * @throws RpcException
     */
    void start(Properties prop, URL url) throws RpcException;

    /**
     * 获取该rpcClient对应的唯一ID
     * @return
     */
    String getId();

    /**
     * 启动完成后可进行请求操作
     * @param request
     * @param timeout
     * @return
     */
    Response request(Request request, Integer timeout);

    /**
     * 获取对应协议
     * @return
     */
    String getProtocol();

    /**
     * 获取host
     * @return
     */
    String getHost();

    /**
     * 获取port
     * @return
     */
    Integer getPort();
}
