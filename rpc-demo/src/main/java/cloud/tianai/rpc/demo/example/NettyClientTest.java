package cloud.tianai.rpc.demo.example;

import cloud.tianai.rpc.remoting.codec.hessian2.Hessian2Codec;
import cloud.tianai.rpc.remoting.api.*;
import cloud.tianai.rpc.remoting.netty.NettyClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyClientTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
//        NettyClient nettyClient = new NettyClient();
//        RemotingServerConfiguration config = new RemotingServerConfiguration();
//
//        config.setHost("127.0.0.1");
//        config.setPort(20881);
//        config.setWorkerThreads(16);
//        config.setCodec(new Hessian2Codec());
//        config.setRemotingDataProcessor(new DemoRemotingDataProcessor());
//        config.setConnectTimeout(3000);
//        RemotingChannelHolder channelHolder = nettyClient.start(config);
//
//        Demo demo = new Demo(1, "哈哈");
//        Request request = new Request();
//        CompletableFuture<Object> future = channelHolder.request(request, 3000);
//
//        Object res = future.get(3000, TimeUnit.SECONDS);
//        System.out.println("res" + res);
    }

    private static class DemoRemotingDataProcessor   implements RemotingDataProcessor {

        @Override
        public void readMessage(Channel channel, Object msg, Object extend) {
            DefaultFuture.received(channel, (Response) msg, true);
        }

        @Override
        public Object writeMessage(Channel channel, Object msg, Object extend) {
            return null;
        }


        @Override
        public void sendHeartbeat(Channel channel, Object extend) {

        }

        @Override
        public void sendError(Channel channel, Throwable ex, Object data) {

        }

        @Override
        public boolean support(Object msg) {
            return true;
        }

        @Override
        public Class<?>[] getSupportParams() {
            return new Class[0];
        }

    }
}
