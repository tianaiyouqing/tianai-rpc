package cloud.tianai.rpc.core.util;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.factory.RegistryFactory;
import cloud.tianai.rpc.registory.api.Registry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryUtils {

    public static Registry createAndStart(URL config) {
        Registry r = RegistryFactory.createRegistry(config.getProtocol());
        // 重试次数
        r.start(config);
        return r;
    }
}
