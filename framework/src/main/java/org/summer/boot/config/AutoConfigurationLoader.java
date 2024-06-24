package org.summer.boot.config;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.reflections.Reflections;
import org.summer.boot.annotations.EnableAutoConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class AutoConfigurationLoader {

    private final Set<String> basePackages;
    private final Injector injector;

    public AutoConfigurationLoader(Set<String> basePackages, Injector injector) {
        this.basePackages = basePackages;
        this.injector = injector;
    }

    public void loadAutoConfigurations() {
        Set<Class<?>> configClasses = new HashSet<>();

        // 扫描基础包中的自动配置类
        for (String basePackage : basePackages) {
            Reflections reflections = new Reflections(basePackage);
            configClasses.addAll(reflections.getTypesAnnotatedWith(EnableAutoConfiguration.class));
        }

        // 加载 starter.factories 中的自动配置类
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("META-INF/starter.factories")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                String autoConfigClasses = properties.getProperty("com.example.autoconfig.EnableAutoConfiguration");
                if (autoConfigClasses != null) {
                    for (String className : autoConfigClasses.split(",")) {
                        configClasses.add(Class.forName(className.trim()));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (Class<?> configClass : configClasses) {
            if (AbstractModule.class.isAssignableFrom(configClass)) {
                injector.getInstance(configClass);
            }
        }
    }
}
