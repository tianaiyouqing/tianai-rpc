package cloud.tianai.rpc.common.util.id;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: 天爱有情
 * @date 2021/8/9 10:33
 * @Description 雪花算法 ID生成器, 优化了一下雪花算法，对于tianai-RPC中使用优化后的雪花算法 位数更短
 */
public class SnowflakeIdGenerator {

    public static final String ID = "Snowflake";

    private final Sequence worker;

    public SnowflakeIdGenerator() {
        this.worker = new Sequence();
    }

    public String generateId(String tag) {
        if (tag == null) {
            return generateId();
        }
        // 可以表示 0 ~ 255 个数用来标识
        long iTag;
        try {
            iTag = Long.parseLong(tag);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("SnowflakeIdGenerator 的 TAG 内容必须是数字");
        }

        return String.valueOf(worker.nextId(iTag));
    }

    public String getCustomIdTag(String id) {
        return String.valueOf(worker.getCustomId(Long.parseLong(id)));
    }


    public String generateId() {
        return String.valueOf(worker.nextId(0));
    }


    public static void main(String[] args) {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator();
        String id = snowflakeIdGenerator.generateId();
        System.out.println(id);
    }

    /**
     * @Author: 天爱有情
     * @Date 2020/8/7 10:26
     * <p>
     * 雪花算法
     * 优化:
     * 将工作机器ID 缩减为 5bit ， 也就是 2^5= 32 , 最多可以部署32个机器
     * 将序列号ID 缩减为 9 bit， 也就是 2^9= 512, 在同一毫秒内可以生成512个ID号，(对于公司项目应该足够用了)
     * <p>
     * 0  -  00000000  -  0000000000 0000000000 0000000000 0000000000 0  -  00000  -  000000000
     * [不用]  [自定义标识]    [               41位bit-时间戳                 ]  [5bit-机器] [9bit-序列号]
     * <p>
     * 预留出8位bit的空间，用来自定义标识一些别的东西， 最大可以表示 0~255 个数
     */
    @Slf4j
    static class Sequence {
        /**
         * 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）.
         */
        private final long twepoch = 1626431482656L;
        /**
         * 机器标识位数
         */
        private final long workerIdBits = 5L;
        private final long maxWorkerId = ~(-1L << workerIdBits);
        /**
         * 毫秒内自增位
         */
        private final long sequenceBits = 9L;
        private final long workerIdShift = sequenceBits;

        /**
         * 自定义的标识位 8 位
         */
        private final long customBits = 8;

        private final long timestampBits = 41;

        /**
         * 时间戳左移动位
         */
        private final long timestampLeftShift = sequenceBits + workerIdBits;

        private final long customLeftShift = timestampBits + timestampLeftShift;
        private final long sequenceMask = ~(-1L << sequenceBits);

        private final long workerId;

        /**
         * 数据标识 ID 部分
         */
        /**
         * 并发控制
         */
        private long sequence = 0L;
        /**
         * 上次生产 ID 时间戳
         */
        private long lastTimestamp = -1L;


        public Sequence() {
            this.workerId = getMaxWorkerId(maxWorkerId);
        }

        /**
         * 获取 maxWorkerId
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

        /**
         * 获取下一个 ID
         *
         * @return 下一个 ID
         */
        public synchronized long nextId(long customId) {
            if (Long.toBinaryString(customId).length() > customBits) {
                throw new RuntimeException("自定义ID标识超出范围， 标识ID范围 0~255");
            }
            long timestamp = timeGen();
            //闰秒
            if (timestamp < lastTimestamp) {
                long offset = lastTimestamp - timestamp;
                if (offset <= 5) {
                    try {
                        wait(offset << 1);
                        timestamp = timeGen();
                        if (timestamp < lastTimestamp) {
                            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", offset));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", offset));
                }
            }

            if (lastTimestamp == timestamp) {
                // 相同毫秒内，序列号自增
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    // 同一毫秒的序列数已经达到最大
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                // 不同毫秒内，序列号置为 1 - 3 随机数
                sequence = ThreadLocalRandom.current().nextLong(1, 3);
            }

            lastTimestamp = timestamp;
//            System.out.println(Long.toBinaryString(customId) +":" + Long.toBinaryString(timestamp - twepoch) + ":" + Long.toBinaryString(workerId) +":" + Long.toBinaryString(sequence));
//            System.out.println(Long.toBinaryString(customId << 55));
            // 时间戳部分 | 机器标识部分 | 序列号部分
//            System.out.println(Long.toBinaryString(System.currentTimeMillis()));
            return (customId << customLeftShift
                    | (timestamp - twepoch) << timestampLeftShift)
                    | (workerId << workerIdShift)
                    | sequence;
        }

        protected long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }

        protected long timeGen() {
            return System.currentTimeMillis();
        }

        public long getCustomId(long id) {
            return id >>> customLeftShift;
        }
    }

}
