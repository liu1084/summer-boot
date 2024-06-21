package org.summer.boot.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class PropertiesConfigManager {

    private Configuration config;

    public PropertiesConfigManager(String filePath) {
        Configurations configs = new Configurations();
        try {
            config = configs.properties(getClass().getClassLoader().getResource(filePath));
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load properties file: " + filePath, e);
        }
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public int getInt(String key) {
        return config.getInt(key);
    }

    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    // 其他类型的获取方法
}
