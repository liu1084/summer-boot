package org.summer.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.summer.boot.annotations.ConfigurationProperties;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.summer.boot.constants.Constant.*;


public class ConfigurationManager {

    private static Map<String, Object> configurations = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private static String activeProfile;
    private static boolean isDebug;


    public static void loadProperties() {
        getDefaultConfigValues();
        // 加载 application.yml 配置
        Logger.debug("load config file {}", DEFAULT_CONFIGURATION_FILE);
        loadYmlProperties(DEFAULT_CONFIGURATION_FILE);
        // 获取活跃的 profile
        // 加载 application-{activeProfile}.yml 配置
        if (StringUtils.isNoneBlank(activeProfile)) {
            loadYmlProperties("application-" + activeProfile + ".yml");
        }
    }


    private static void loadYmlProperties(String fileName) {
        try (InputStream input = ConfigurationManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                Yaml yaml = new Yaml(new Constructor(Map.class));
                Iterable<Object> yamlDocuments = yaml.loadAll(input);
                for (Object document : yamlDocuments) {
                    if (document instanceof Map) {
                        mergeProperties((Map<String, Object>) document, configurations);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mergeProperties(Map<String, Object> source, Map<String, Object> target) {
        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (value instanceof Map && target.get(key) instanceof Map) {
                mergeProperties((Map<String, Object>) value, (Map<String, Object>) target.get(key));
            } else {
                target.put(key, value);
            }
        }
    }


    private static void getDefaultConfigValues() {
        try (InputStream input = ConfigurationManager.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIGURATION_FILE)) {
            if (input != null) {
                Map<String, Object> yamlMap = mapper.readValue(input, Map.class);
                activeProfile = (String) yamlMap.getOrDefault("app.profiles.active", DEFAULT_ACTIVE_PROFILE);
                isDebug = (boolean) yamlMap.getOrDefault("app.isDebug", DEFAULT_IS_DEBUG);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private static <T> void bindProperties(String prefix, T instance) {
        Map<String, Object> prefixedProperties = getPrefixedProperties(prefix, configurations);
        if (prefixedProperties != null) {
            for (Field field : instance.getClass().getDeclaredFields()) {
                String key = field.getName();
                Object value = prefixedProperties.get(key);
                if (value != null) {
                    field.setAccessible(true);
                    try {
                        field.set(instance, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static Map<String, Object> getPrefixedProperties(String prefix, Map<String, Object> properties) {
        String[] keys = prefix.split("\\.");
        Map<String, Object> currentMap = properties;
        for (String key : keys) {
            Object value = currentMap.get(key);
            if (value instanceof Map) {
                currentMap = (Map<String, Object>) value;
            } else {
                return null;
            }
        }
        return currentMap;
    }

    private static void loadYamlFile(String fileName) {
        try (InputStream input = ConfigurationManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                Map<String, Object> yamlMap = mapper.readValue(input, Map.class);
                configurations.putAll(yamlMap);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static <T> T getConfiguration(Class<T> configClass) {
        try {
            String json = mapper.writeValueAsString(configurations);
            return mapper.readValue(json, configClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bind configuration", e);
        }
    }

    public static Object getProperty(String key) {
        return configurations.get(key);
    }
}
