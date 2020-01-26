package cloud.tianai.rpc.common;

import lombok.Data;

import java.util.Objects;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/26 13:54
 * @Description: 单个 keyvalue容器
 */
@Data
public class KeyValue<K,V> {

    private K key;
    private V value;

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public boolean isNotEmpty() {
        return key != null && value != null;
    }
}
