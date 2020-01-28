package cloud.tianai.rpc.core.client.impl;

import cloud.tianai.remoting.api.*;
import cloud.tianai.rpc.common.KeyValue;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.id.IdUtils;
import cloud.tianai.rpc.core.client.RpcClient;
import cloud.tianai.rpc.core.constant.RpcClientConfigConstant;
import cloud.tianai.rpc.core.constant.RpcConfigConstant;
import cloud.tianai.rpc.core.constant.RpcServerConfigConstant;
import cloud.tianai.rpc.core.factory.RemotingClientFactory;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataDecoder;
import cloud.tianai.rpc.remoting.codec.api.RemotingDataEncoder;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static cloud.tianai.rpc.core.factory.CodecFactory.getCodec;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/27 15:07
 * @Description: Rpc客户端实现
 */
public class RpcClientImpl implements RpcClient, RpcInvocation {
    /** 远程客户端. */
    private RemotingClient remotingClient;
    /** 远程管道持有者. */
    private RemotingChannelHolder channelHolder;
    /** 远程客户端配置. */
    private RemotingConfiguration remotingConfiguration;
    /** host. */
    private String host;
    /** 端口. */
    private Integer port;
    /** 对应的协议. */
    private String protocol;
    /** 当前URL. */
    private URL url;
    /** prop参数(用户自定义). */
    private Properties prop;
    /** 唯一ID. */
    private String id;

    public RpcClientImpl() {
    }

    public RpcClientImpl(String id) {
        this.id = id;
    }

    @Override
    public void start(Properties prop, URL url) throws RpcException {
        this.url = url;
        this.prop = prop;
        if(id == null) {
            this.id = IdUtils.getNoRepetitionIdStr();
        }
        // 启动远程链接
        startRemotingClient();
    }

    @Override
    public String getId() {
        return id;
    }


    /**
     * 启动远程客户端
     */
    private void startRemotingClient() {
        // 创建远程客户端
        protocol = prop.getProperty(RpcClientConfigConstant.PROTOCOL, RpcClientConfigConstant.DEFAULT_PROTOCOL);
        remotingClient = RemotingClientFactory.create(protocol);
        if(Objects.isNull(remotingClient)) {
            throw new RpcException("未找到对应的远程server, protocol=" + protocol);
        }
        RemotingConfiguration conf = readRemotingClientConfiguration();
        this.remotingConfiguration = conf;
        this.host = conf.getHost();
        this.port = conf.getPort();
        channelHolder = remotingClient.start(conf);
    }

    /**
     * 读取远程客户端参数
     * @return
     */
    private RemotingConfiguration readRemotingClientConfiguration() {
        String host = url.getHost();
        if(StringUtils.isBlank(host)) {
            throw new RpcException("客户端启动失败，必须指定host");
        }
        Integer port = url.getPort();
        int workThreads = Integer.parseInt(url.getParameter(RpcClientConfigConstant.WORKER_THREADS, String.valueOf(RpcConfigConstant.DEFAULT_IO_THREADS)));

        String codecProtocol = prop.getProperty(RpcClientConfigConstant.CODEC, RpcClientConfigConstant.DEFAULT_CODEC);
        KeyValue<RemotingDataEncoder, RemotingDataDecoder> codec = getCodec(codecProtocol);
        if(codec == null || !codec.isNotEmpty()) {
            throw new RpcException("未找到对应的codec， protocol=" + codecProtocol);
        }
        Integer timeout = Integer.valueOf(prop.getProperty(RpcClientConfigConstant.TIMEOUT, String.valueOf(5000)));
        RemotingConfiguration conf = new RemotingConfiguration();
        conf.setHost(host);
        conf.setPort(port);
        conf.setWorkerThreads(workThreads);
        conf.setEncoder(codec.getKey());
        conf.setDecoder(codec.getValue());
        conf.setConnectTimeout(timeout);
        conf.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(this));
        return conf;
    }


    /**
     * 执行请求
     * @param request 请求体
     * @param timeout 请求超时
     * @return Response
     */
    @Override
    public Response request(Request request, Integer timeout) {
        CompletableFuture<Object> future = channelHolder.request(request, timeout);
        Response response;

        try {
            Object invokeRes = future.get(timeout, TimeUnit.MILLISECONDS);
            if(invokeRes instanceof Response) {
                response = (Response) invokeRes;
            }else {
                response = new Response(request.getId());
                response.setResult(response);
                response.setStatus(Response.OK);
            }
        } catch (InterruptedException e) {
            // server端超时
            e.printStackTrace();
            response = new Response(request.getId());
            response.setErrorMessage(e.getMessage());
            response.setStatus(Response.SERVER_TIMEOUT);
        } catch (ExecutionException e) {
            e.printStackTrace();
            response = new Response(request.getId());
            response.setErrorMessage(e.getMessage());
            response.setStatus(Response.BAD_RESPONSE);
        } catch (TimeoutException e) {
            // 超时.
            e.printStackTrace();
            response = new Response(request.getId());
            response.setErrorMessage(e.getMessage());
            response.setStatus(Response.CLIENT_TIMEOUT);
        }

        return response;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public Object invoke(Request request) {
        // 客户端不处理请求数据
        if (request.isHeartbeat()) {
            return "heartbeat success";
        }
        return null;
    }
}
