package cloud.tianai.rpc.common.sort;

import cloud.tianai.rpc.common.util.ObjectUtils;

import java.util.Comparator;

/**
 * @Author: 天爱有情
 * @Date: 2020/03/03 15:19
 * @Description: 基于Ordered接口的比较器
 */
public class OrderComparator implements Comparator<Object> {

    /**
     * 默认实例
     */
    public static final OrderComparator INSTANCE = new OrderComparator();


    @Override
    public int compare(Object o1, Object o2) {
        return doCompare(o1, o2, null);
    }

    private int doCompare(Object o1, Object o2, OrderSourceProvider sourceProvider) {
        int i1 = getOrder(o1, sourceProvider);
        int i2 = getOrder(o2, sourceProvider);
        return Integer.compare(i1, i2);
    }

    /**
     * 获取order 顺序, 通过 Ordered接口读取，如果没有实现Order接口则默认为 Ordered.LOWEST_PRECEDENCE
     *
     * @param obj
     * @param sourceProvider
     * @return
     */
    private int getOrder(Object obj, OrderSourceProvider sourceProvider) {
        Integer order = null;
        if (obj != null && sourceProvider != null) {
            Object orderSource = sourceProvider.getOrderSource(obj);
            if (orderSource != null) {
                if (orderSource.getClass().isArray()) {
                    Object[] sources = ObjectUtils.toObjectArray(orderSource);
                    for (Object source : sources) {
                        order = findOrder(source);
                        if (order != null) {
                            break;
                        }
                    }
                } else {
                    order = findOrder(orderSource);
                }
            }
        }
        return (order != null ? order : getOrder(obj));
    }

    /**
     * 获取Order顺序ID， 通过Ordered接口实现
     *
     * @param obj
     * @return
     */
    protected int getOrder(Object obj) {
        if (obj != null) {
            Integer order = findOrder(obj);
            if (order != null) {
                return order;
            }
        }
        return 0;
    }

    /**
     * 通过Ordered接口读取Order
     * 顺序信息
     *
     * @param obj
     * @return
     */
    protected Integer findOrder(Object obj) {
        return (obj instanceof Ordered ? ((Ordered) obj).getOrder() : null);
    }


    /**
     * 策略界面，用于为给定对象提供顺序来源。
     */
    @FunctionalInterface
    public interface OrderSourceProvider {

        /**
         * Return an order source for the specified object, i.e. an object that
         * should be checked for an order value as a replacement to the given object.
         * <p>Can also be an array of order source objects.
         * <p>If the returned object does not indicate any order, the comparator
         * will fall back to checking the original object.
         *
         * @param obj the object to find an order source for
         * @return the order source for that object, or {@code null} if none found
         */

        Object getOrderSource(Object obj);
    }

}