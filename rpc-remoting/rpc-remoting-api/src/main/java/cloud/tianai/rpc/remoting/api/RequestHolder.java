package cloud.tianai.rpc.remoting.api;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/23 22:00
 * @Description: Request持有者
 */
public class RequestHolder {

    private static final ThreadLocal<Request> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    public static Request get() {
        Request request = REQUEST_THREAD_LOCAL.get();
        return request;
    }

    public static Request getOrCreate() {
        Request request = get();
        if(request == null) {
            // 如果等于空
            request = new Request();
            set(request);
        }
        return request;
    }

    public static void set(Request request) {
        REQUEST_THREAD_LOCAL.set(request);
    }

    public static void remove() {
        REQUEST_THREAD_LOCAL.remove();
    }

}
