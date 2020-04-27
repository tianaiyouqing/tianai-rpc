package cloud.tianai.rpc.demo.api;

import cloud.tianai.rpc.remoting.codec.hessian2.Hessian2Codec;
import cloud.tianai.rpc.remoting.netty.NettyClient;
import cloud.tianai.rpc.remoting.api.RemotingChannelHolder;
import cloud.tianai.rpc.remoting.api.RemotingServerConfiguration;
import cloud.tianai.rpc.remoting.api.RequestResponseRemotingDataProcessor;
import cloud.tianai.rpc.remoting.api.RpcInvocation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class NettyClienTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
//        NettyClient nettyClient = new NettyClient();
//        RemotingServerConfiguration config = new RemotingServerConfiguration();
//
//        config.setHost("127.0.0.1");
//        config.setPort(20881);
//        config.setWorkerThreads(16);
//        config.setCodec(new Hessian2Codec());
//        RpcInvocation rpcInvocation = new NettyServerTest.TestRpcInvocation();
//        config.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(rpcInvocation));
//        config.setConnectTimeout(3000);
//        RemotingChannelHolder channelHolder = nettyClient.start(config);
//
//        DemoService demoService = new TestProxy().newProxy(DemoService.class, channelHolder);
//        DemoRequest demoRequest = new DemoRequest();
//        demoRequest.setPrice(100d);
//        DemoResult result = demoService.req("Hi好", 1, demoRequest);
//        System.out.println("请求返回:" + result);
    }
}
