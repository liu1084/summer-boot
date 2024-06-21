package org.summer.boot.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlConfigManager {

    private Map<String, Object> config;

    public YamlConfigManager(String filePath) {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (in != null) {
                config = yaml.load(in);
            } else {
                throw new RuntimeException("YAML file not found: " + filePath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + filePath, e);
        }
    }

    public Object get(String key) {
        return config.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) config.get(key);
    }
}

