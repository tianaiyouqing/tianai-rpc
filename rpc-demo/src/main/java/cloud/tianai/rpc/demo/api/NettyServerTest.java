package cloud.tianai.rpc.demo.api;

import cloud.tianai.rpc.remoting.api.*;
import cloud.tianai.rpc.remoting.api.util.ResponseUtils;
import cloud.tianai.rpc.remoting.codec.hessian2.Hessian2Codec;
import cloud.tianai.rpc.remoting.netty.NettyServer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * @author Administrator
 */
public class NettyServerTest {

    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
//        NettyServer nettyServer = new NettyServer();
//        RemotingServerConfiguration config = new RemotingServerConfiguration();
//        config.setHost("127.0.0.1");
//        config.setPort(20881);
//        config.setWorkerThreads(16);
//        config.setCodec(new Hessian2Codec());
//        RpcInvocation rpcInvocation = new TestRpcInvocation();
//        config.setRemotingDataProcessor(new RequestResponseRemotingDataProcessor(rpcInvocation));
//        config.setIdleTimeout(60000);
//        config.setBossThreads(1);
//        RemotingChannelHolder channelHolder = nettyServer.start(config);
//        System.out.println("nettyServer启动成功");
//        // 阻塞
//        countDownLatch.await();
    }

    public static class TestRpcInvocation implements RpcInvocation {

        @Override
        public Response invoke(Request request) {
            System.out.println("接口类型: " + request.getInterfaceType());
            try {
                Class<?> clazz = Class.forName("cloud.tianai.rpc.demo.api.DemoServiceImpl");
                Object[] requestParam = request.getRequestParam();
                Class<?>[] requestParamType = new Class<?>[requestParam.length];
                for (int i = 0; i < requestParam.length; i++) {
                    requestParamType[i] = requestParam[i].getClass();
                }
                Method method = clazz.getMethod(request.getMethodName(), requestParamType);

                Object obj = clazz.newInstance();
                Object result = method.invoke(obj, requestParam);
                return ResponseUtils.warpResponse(result, request);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
