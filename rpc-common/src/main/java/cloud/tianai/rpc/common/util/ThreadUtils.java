package cloud.tianai.rpc.common.util;

import java.util.concurrent.TimeUnit;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 14:48
 * @Description: 线程相关工具包
 */
public class ThreadUtils {

    public static void sleep(long millis, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(millis);
        } catch (InterruptedException e) {
            // 不做处理
        }
    }
}
