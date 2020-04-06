package cloud.tianai.rpc.common.extension;

import java.lang.annotation.*;


/**
 * @Author: 天爱有情
 * @Date: 2020/04/06 12:36
 * @Description: SPI扩展必须指定该注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    String value() default "";
}
