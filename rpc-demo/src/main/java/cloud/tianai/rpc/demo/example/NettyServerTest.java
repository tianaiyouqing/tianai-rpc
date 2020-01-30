package cloud.tianai.rpc.demo.example;

import cloud.tianai.remoting.api.*;
import cloud.tianai.remoting.codec.hessian2.Hessian2Decoder;
import cloud.tianai.remoting.codec.hessian2.Hessian2Encoder;
import cloud.tianai.remoting.netty.NettyServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NettyServerTest {

    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        NettyServer nettyServer = new NettyServer();

        RemotingServerConfiguration config = new RemotingServerConfiguration();

        config.setHost("127.0.0.1");
        config.setPort(20881);
        config.setWorkerThreads(16);
        config.setEncoder(new Hessian2Encoder());
        config.setDecoder(new Hessian2Decoder());
        config.setRemotingDataProcessor(new DemoRemotingDataProcessor());
        config.setIdleTimeout(60000);
        config.setBossThreads(1);

        RemotingChannelHolder channelHolder = nettyServer.start(config);

        System.out.println("nettyServer启动成功");
        // 阻塞
        countDownLatch.await();
    }


    private static class DemoRemotingDataProcessor implements RemotingDataProcessor {

        @Override
        public void readMessage(cloud.tianai.remoting.api.Channel channel, Object msg, Object extend) {
            Request request = (Request) msg;
            System.out.println("Server端收到消息:" + msg);
            Response response = new Response(request.getId(), "v1");
            response.setResult(new Demo(2, "hello客户端"));
            channel.write(response);
        }

        @Override
        public Object writeMessage(cloud.tianai.remoting.api.Channel channel, Object msg, Object extend) {
            return null;
        }

        @Override
        public void sendHeartbeat(Channel channel, Object extend) {

        }

        @Override
        public boolean support(Object msg) {
            return false;
        }
    }
}
