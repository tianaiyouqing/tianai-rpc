package cloud.tianai.rpc.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/21 23:12
 * @Description: 统一返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 成功的code码. */
    public static final Integer SUCCESS_CODE = 200;
    /** 失败的code码. */
    public static final Integer ERROR_CODE = 500;

    private Integer code;
    private String msg;
    private T data;


    public static <T> Result<T> ofSuccess(T data) {
        return new Result<T>(SUCCESS_CODE, null, data);
    }

    public static <T> Result<T> ofMessage(Integer code, String msg) {
        return new Result<T>(code, msg, null);
    }

    public static <T> Result<T> ofError(String msg) {
        return new Result<T>(ERROR_CODE, msg, null);
    }

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

    public boolean compare(Integer code) {
        return Objects.equals(this.code, code);
    }

    public Result<T> compare(Integer code, Consumer<T> consumer) {
        if(compare(code)) {
            consumer.accept(data);
        }
        return this;
    }
}
