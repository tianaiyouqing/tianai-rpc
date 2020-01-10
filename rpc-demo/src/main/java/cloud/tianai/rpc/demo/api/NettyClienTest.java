package cloud.tianai.rpc.demo.api;

import cloud.tianai.remoting.api.*;
import cloud.tianai.remoting.codec.hessian2.Hessian2Decoder;
import cloud.tianai.remoting.codec.hessian2.Hessian2Encoder;
import cloud.tianai.remoting.netty.NettyClient;
import cloud.tianai.rpc.demo.example.Demo;
import cloud.tianai.rpc.demo.example.NettyClientTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyClienTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        NettyClient nettyClient = new NettyClient();
        RemotingServerConfiguration config = new RemotingServerConfiguration();

        config.setHost("127.0.0.1");
        config.setPort(20881);
        config.setWorkerThreads(16);
        config.setEncoder(new Hessian2Encoder());
        config.setDecoder(new Hessian2Decoder());
        RpcInvocation rpcInvocation = new NettyServerTest.TestRpcInvocation();
        config.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(rpcInvocation));
        config.setConnectTimeout(3000);
        RemotingChannelHolder channelHolder = nettyClient.start(config);

        DemoService demoService = new TestProxy().newProxy(DemoService.class, channelHolder);
        DemoRequest demoRequest = new DemoRequest();
        demoRequest.setPrice(100d);
        DemoResult result = demoService.req("Hi好", 1, demoRequest);
        System.out.println("请求返回:" + result);
    }
}
