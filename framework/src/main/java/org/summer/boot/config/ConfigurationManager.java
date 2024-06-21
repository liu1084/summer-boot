package org.summer.boot.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    private static final Map<Class<?>, Object> configMap = new HashMap<>();

    public static void addConfiguration(Class<?> configClass, Object configInstance) {
        configMap.put(configClass, configInstance);
    }

    public static <T> T getConfiguration(Class<T> configClass) {
        return configClass.cast(configMap.get(configClass));
    }
}
