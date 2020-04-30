package cloud.tianai.rpc.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.CodeSource;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/30 19:21
 * @Description: rpc版本号获取
 */
@Slf4j
public class RpcVersion {

    private static final String VERSION = getVersion(RpcVersion.class, "");

    public static String getVersion() {
        return VERSION;
    }

    /**
     * 获取版本号
     * @param cls 通过 class的ClassLoader路径
     * @param defaultVersion 默认版本号
     * @return 版本号
     */
    public static String getVersion(Class<?> cls, String defaultVersion) {
        Package pkg = RpcVersion.class.getPackage();
        String version = null;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
            if (StringUtils.isNotEmpty(version)) {
                return version;
            }
            version = pkg.getSpecificationVersion();
            if (StringUtils.isNotEmpty(version)) {
                return version;
            }
        }

        CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            log.info("No codeSource for class " + cls.getName() + " when getVersion, use default version " + defaultVersion);
            return defaultVersion;
        }

        String file = codeSource.getLocation().getFile();
        if (!StringUtils.isEmpty(file) && file.endsWith(".jar")) {
            version = getFromFile(file);
        }
        // return default version if no version info is found
        return StringUtils.isEmpty(version) ? defaultVersion : version;
    }

    /**
     * get version from file: path/to/group-module-x.y.z.jar, returns x.y.z
     */
    private static String getFromFile(String file) {
        // remove suffix ".jar": "path/to/group-module-x.y.z"
        file = file.substring(0, file.length() - 4);

        // remove path: "group-module-x.y.z"
        int i = file.lastIndexOf('/');
        if (i >= 0) {
            file = file.substring(i + 1);
        }

        // remove group: "module-x.y.z"
        i = file.indexOf("-");
        if (i >= 0) {
            file = file.substring(i + 1);
        }

        // remove module: "x.y.z"
        while (file.length() > 0 && !Character.isDigit(file.charAt(0))) {
            i = file.indexOf("-");
            if (i >= 0) {
                file = file.substring(i + 1);
            } else {
                break;
            }
        }
        return file;
    }


    public static void main(String[] args) {
        System.out.println(RpcVersion.getVersion());
    }
}
