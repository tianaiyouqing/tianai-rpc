package cloud.tianai.rpc.common.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/27 22:10 
 * @Description: 排它锁实现
 */
public class RpcReentrantLock implements RpcLock{

    /** NonfairSync. */
    private ReentrantLock normalLock = new ReentrantLock();

    @Override
    public void lock() {
        //noinspection AlibabaLockShouldWithTryFinally
        normalLock.lock();
    }

    @Override
    public void unlock() {
        normalLock.unlock();
    }
}
