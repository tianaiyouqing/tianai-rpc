package cloud.tianai.rpc.demo.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
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
    public static void main(String[] args) throws InterruptedException, IOException {
        URL resources = Demo.class.getClassLoader().getResource("dubbo/");
        String filePath = resources.getFile();
        File file = new File(filePath);

//        Properties prop = new Properties();
//        prop.load(inputStream);
//
        System.out.println(resources);
    }
}
