package cloud.tianai.rpc.common.lock;


import java.util.concurrent.TimeUnit;

public class RpcSpinLockTest {


    public static void main(String[] args) throws InterruptedException {
        RpcLock rpcLock = new RpcSpinLock();

        new Thread(() -> {
           rpcLock.lock();
            System.out.println("thread1 locked");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thread1 sleep finish");

            rpcLock.unlock();
            System.out.println("thread1 unlock");
        }).start();


        new Thread(() -> {
            rpcLock.lock();
            System.out.println("thread2 locked");
            rpcLock.unlock();
            System.out.println("thread2 unlock");
        }).start();


        TimeUnit.HOURS.sleep(1);

    }

}