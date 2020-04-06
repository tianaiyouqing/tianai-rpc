package cloud.tianai.rpc.registory.api;

import cloud.tianai.rpc.common.Result;
import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.common.extension.SPI;

import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/21 23:23
 * @Description: 注册器
 */
@SPI
public interface Registry {

    /**
     * 注册一个地址
     * @param url 代注册的地址
     * @return 返回是否注册成功
     */
    Result<?> register(URL url);

    /**
     * 读取某个地址下的远程地址列表
     * @param url
     * @return
     */
    Result<List<URL>> lookup(URL url);

    /**
     * 是否已启动
     * @return
     */
    boolean isStart();

    String getProtocol();
    /**
     * 订阅
     * @param url
     * @param listener
     */
    void subscribe(URL url, NotifyListener listener);

    /**
     * 订阅registry的状态
     * @param statusListener
     */
    void subscribe(StatusListener statusListener);

    /**
     * 取消订阅
     * @param url
     * @param listener
     */
    void unsubscribe(URL url, NotifyListener listener);

    /**
     * 初始化
     * @param url
     */
    Registry start(URL url);

    /**
     * 关闭
     */
    void shutdown();
}
