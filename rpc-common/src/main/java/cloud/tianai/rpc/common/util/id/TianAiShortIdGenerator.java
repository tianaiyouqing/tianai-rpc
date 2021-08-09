package cloud.tianai.rpc.common.util.id;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;


/**
 * @Author: 天爱有情
 * @date 2021/8/9 10:34
 * @Description 短ID生成， 最慢1秒 1000个ID，该ID的设计思路充分利用未使用的时间进行生成，
 * 一段时间不使用该ID，并发率越高
 * <p>
 * 自研ID，效率杠杠的，算法简单易用
 * <p>
 */
public class TianAiShortIdGenerator {
    public static final String ID = "ShortId";
    /**
     * 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）.
     */
    private final long twepoch = 1626431482656L;
    /** 上一个最后的时间戳. */
    private long lastTimeMillis;
    /** 机器ID. */
    private long workerId;

    public TianAiShortIdGenerator() {
        lastTimeMillis = System.currentTimeMillis();
        workerId = getMaxWorkerId(5);
    }

    /**
     * 获取机器ID ,copy至mybatis
     *
     * @param maxWorkerId
     * @return
     */
    protected static long getMaxWorkerId(long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isNotBlank(name)) {
            /*
             * GET jvmPid
             */
            mpid.append(name.split("@")[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    public Long generateId(String tag) {
        return doGenerateId();
    }

    public Long generateId() {
        return doGenerateId();
    }

    /**
     * @return
     */
    @SneakyThrows
    public synchronized long doGenerateId() {
        long currentTimeMillis;
        while (lastTimeMillis + 1 >= (currentTimeMillis = System.currentTimeMillis())) {
            Thread.sleep(1);
            lastTimeMillis = currentTimeMillis - 1;
        }
        lastTimeMillis++;
        return (lastTimeMillis - twepoch) << 5 | workerId;
    }
}
