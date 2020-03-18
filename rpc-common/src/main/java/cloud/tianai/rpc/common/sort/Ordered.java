package cloud.tianai.rpc.common.sort;

/**
 * @Author: 天爱有情
 * @Date: 2020/03/03 15:10
 * @Description: 排序接口
 */
public interface Ordered {
    /** 默认最小数值， 预留 10个数的空间. */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /** 默认最大数值， 预留 10个数的空间. */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * 数字越小优先级越高
     * @return
     */
    int getOrder();
}
