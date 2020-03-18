package cloud.tianai.remoting.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/23 21:27
 * @Description: 头信息
 */
public class Header implements Serializable {
    /**
     * 存储头信息内容.
     */
    private Map<String, Object> headers = null;

    public Object setHeader(String key, Object value) {
        if (headers == null) {
            headers = new HashMap<>(8);
        }
        Object oldValue = headers.remove(key);
        headers.put(key, value);
        return oldValue;
    }

    public Object getHeader(String key) {
        if (headers == null) {
            return null;
        }
        return headers.get(key);
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getHeaders() {
        if (headers == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> unmodifiableMap = Collections.unmodifiableMap(headers);
        return unmodifiableMap;
    }

    public Object removeHeader(String key) {
        if (headers == null) {
            return null;
        }
        Object removeValue = headers.remove(key);
        return removeValue;
    }
}
