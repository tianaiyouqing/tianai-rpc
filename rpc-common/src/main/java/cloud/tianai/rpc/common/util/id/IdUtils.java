package cloud.tianai.rpc.common.util.id;

import java.util.UUID;

/**
 * Title: IdUtils
 * Description: ID工具包
 *
 * @author: 天爱有情
 * @date: 2018/11/20 14:39
 **/
public class IdUtils {


    private static final SnowflakeIdGenerator SNOWFLAKE_ID_GENERATOR = new SnowflakeIdGenerator();
    private static final Sequence SEQUENCE = new Sequence();

    public static String getIdForSnowflake() {
        return SNOWFLAKE_ID_GENERATOR.generateId();
    }


    public static String getNoRepetitionIdStr() {
        return new String(String.valueOf(SEQUENCE.nextId()));
    }

    /**
     * <p>
     * 获取去掉"-" UUID
     * </p>
     */
    public static synchronized String get32uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
