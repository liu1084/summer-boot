package org.summer.boot.config;

import org.reflections.Reflections;
import org.summer.boot.annotations.ConfigurationProperties;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 用于扫描和绑定配置项到相应的类
 */
public class ConfigBinder {

    private final Properties properties = new Properties();
    private Map<String, Object> yamlConfig;

    public ConfigBinder() {
        loadPropertiesConfig();
        loadYamlConfig();
    }

    private void loadPropertiesConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadYamlConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            if (inputStream != null) {
                Yaml yaml = new Yaml();
                yamlConfig = yaml.load(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bindConfigurations(String[] basePackages) {
        for (String basePackage : basePackages) {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(ConfigurationProperties.class);

            for (Class<?> configClass : annotatedClasses) {
                ConfigurationProperties annotation = configClass.getAnnotation(ConfigurationProperties.class);
                String prefix = annotation.prefix();
                bindProperties(configClass, prefix);
            }
        }
    }

    private void bindProperties(Class<?> configClass, String prefix) {
        try {
            Object configInstance = configClass.getDeclaredConstructor().newInstance();
            for (Field field : configClass.getDeclaredFields()) {
                field.setAccessible(true);
                String key = prefix + "." + field.getName();
                Object value = getConfigValue(key, field.getType());
                if (value != null) {
                    field.set(configInstance, value);
                }
            }
            // Store the config instance for later use
            ConfigurationManager.addConfiguration(configClass, configInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getConfigValue(String key, Class<?> fieldType) {
        String value = properties.getProperty(key);
        if (value == null && yamlConfig != null) {
            value = getYamlValue(key, yamlConfig);
        }
        if (value == null) {
            return null;
        }
        return convertValue(value, fieldType);
    }

    private String getYamlValue(String key, Map<String, Object> yamlMap) {
        String[] parts = key.split("\\.");
        Object current = yamlMap;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current != null ? current.toString() : null;
    }

    private Object convertValue(String value, Class<?> fieldType) {
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(value);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value);
        }
        return null;
    }
}
