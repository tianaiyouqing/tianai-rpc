package cloud.tianai.rpc.common.lock;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/27 22:06
 * @Description: 锁的抽象
 */
public interface RpcLock {

    /**
     * 加锁
     */
    void lock();

    /**
     * 解锁
     */
    void unlock();
}
