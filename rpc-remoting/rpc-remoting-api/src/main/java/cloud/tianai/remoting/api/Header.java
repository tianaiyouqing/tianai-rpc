package cloud.tianai.remoting.api;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/23 21:27
 * @Description: 头信息
 */
@Data
public class Header implements Serializable {
    /** 存储头信息内容. */
    private Map<String, Object> headerMap = new ConcurrentHashMap<>();

    public Object setHeader(String key, Object value) {
        Object oldValue = headerMap.remove(key);
        headerMap.put(key, value);
        return oldValue;
    }

    public Object getHeader(String key) {
        return headerMap.get(key);
    }

    private Map<String, Object> getHeaders() {
        Map<String, Object> unmodifiableMap = Collections.unmodifiableMap(headerMap);
        return unmodifiableMap;
    }

    public Object removeHeader(String key) {
        Object removeValue = headerMap.remove(key);
        return removeValue;
    }
}
