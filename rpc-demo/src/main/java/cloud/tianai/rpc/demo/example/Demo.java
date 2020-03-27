package cloud.tianai.rpc.demo.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Demo implements Serializable {
    private Integer id;
    private String name;


    private static final ReentrantLock lock = new ReentrantLock();
    private static final  Condition c = lock.newCondition();
    public static final LinkedBlockingQueue QUEUE = new LinkedBlockingQueue(100);
    public static void main(String[] args) throws InterruptedException {

//        new Thread(() -> {
//            try {
//                System.out.println("lock begin");
//                lock.lockInterruptibly();
//                System.out.println("lock end");
//                c.await();
//                System.out.println("Condition await");
//
//                c.signal();
//                System.out.println("Condition signal");
//            } catch (InterruptedException e) {
//                lock.unlock();
//                e.printStackTrace();
//            }
//        }).start();
//
//
//        new Thread(() -> {
//            try {
//                System.out.println("lock begin");
//                lock.lockInterruptibly();
//                System.out.println("lock end");
////                c.await();
////                System.out.println("Condition await");
//
//                c.signal();
//                System.out.println("Condition signal");
//            } catch (InterruptedException e) {
//                lock.unlock();
//                e.printStackTrace();
//            }
//        }).start();

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                Object res = null;
                try {
                    res = QUEUE.poll(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(res);

            }).start();
        }
        Thread.sleep(100000);
    }
}
