package cloud.tianai.rpc.core.util;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.constant.CommonConstant;
import cloud.tianai.rpc.core.factory.RegistryFactory;
import cloud.tianai.rpc.registory.api.Registry;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RegistryUtils {

    public static final int DEFAULT_RETRY = 3;

    public static Registry createAndStart(URL config) {
        Registry r = RegistryFactory.createRegistry(config.getProtocol());
        // 重试次数
        Integer retry = config.getParameter(CommonConstant.RETRY, DEFAULT_RETRY);
        // 启动服务注册
        // 超时怎么办???
        Integer index = 0;
        Exception exception = null;
        while (index <= retry) {
            index++;
            try {
                r.start(config);
            } catch (Exception e) {
                exception = e;
                log.error("链接registry失败，尝试重连", e);
            }
            if (r.isStart()) {
                // 如果已经启动，直接跳出循环
                break;
            }
            // 如果没有启动， sleep一段时间再执行启动
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                // 不做处理
            }
        }
        if (!r.isStart()) {
            // 如果没有启动,直接抛出异常
            if (exception != null) {
                exception.printStackTrace();
                throw new RpcException("启动registy失败，e=" + exception.getLocalizedMessage());
            } else {
                throw new RpcException("启动registry失败.");
            }
        }
        return r;
    }
}
