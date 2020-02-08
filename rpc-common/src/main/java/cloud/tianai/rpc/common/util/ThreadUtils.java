package cloud.tianai.rpc.common.util;

import java.util.concurrent.TimeUnit;

public class ThreadUtils {

    public static void sleep(long millis, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(millis);
        } catch (InterruptedException e) {
            // 不做处理
        }
    }
}
