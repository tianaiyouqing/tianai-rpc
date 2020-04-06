package cloud.tianai.rpc.common.util;

import cloud.tianai.rpc.common.io.UnsafeStringWriter;

import java.io.PrintWriter;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/06 13:08
 * @Description: 异常工具包
 */
public class ExceptionUtils {


    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }
}
