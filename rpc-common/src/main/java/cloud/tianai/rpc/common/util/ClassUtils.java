package cloud.tianai.rpc.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClassUtils {

    public static <T> T createObject(Class<? extends T> clazz, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (clazz.isInterface()) {
            throw new InstantiationException("该class是个接口，不可实例化");
        }
        Class<?>[] types = getType(args);
        Constructor<? extends T> constructor = clazz.getConstructor(types);
        T res = constructor.newInstance(args);
        return res;
    }

    public static Class<?>[] getType(Object[] params) {
        if (params == null || params.length < 1) {
            return new Class<?>[0];
        }
        Class<?>[] classArr = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> clazz = params[i].getClass();
            classArr[i] = clazz;
        }

        return classArr;
    }

    public static Class<?> forName(String classStr, ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(classStr);
        return clazz;
    }

    public static Class<?> forName(String classStr) throws ClassNotFoundException {
        return forName(classStr, Thread.currentThread().getContextClassLoader());
    }

    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = null;
        try {
             classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
        }
        return classLoader;
    }

    public static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = getClassLoader();
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = clazz.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }

        return cl;
    }

    public static ClassLoader getCallerClassLoader(Class<?> caller) {
        return caller.getClassLoader();
    }
}
