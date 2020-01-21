package cloud.tianai.rpc.registory.api;


import cloud.tianai.rpc.common.URL;

/**
 * Registry注册工厂
 */
public interface RegistryFactory {

    /**
     * 获取注册工厂
     * @param url
     * @return
     */
    Registry getRegistry(URL url);
}