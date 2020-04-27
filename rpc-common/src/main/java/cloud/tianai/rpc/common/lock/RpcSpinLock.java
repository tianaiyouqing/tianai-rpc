package cloud.tianai.rpc.common.lock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/27 22:08
 * @Description: 自旋锁实现
 */
public class RpcSpinLock implements RpcLock {

    /** true: Can lock, false : in lock.. */
    private AtomicBoolean spinLock = new AtomicBoolean(true);

    @Override
    public void lock() {
        boolean flag;
        do {
            flag = this.spinLock.compareAndSet(true, false);
        }
        while (!flag);
    }

    @Override
    public void unlock() {
        this.spinLock.compareAndSet(false, true);
    }
}
