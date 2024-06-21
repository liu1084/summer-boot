package org.summer.boot.config;

import java.io.IOException;
import java.util.Set;
import org.reflections.Reflections;
import org.summer.boot.annotations.Configuration;

public class AutoConfigurationLoader {

    public static void loadConfigurations() {
        Reflections reflections = new Reflections("your.package.name");
        Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(Configuration.class);
        for (Class<?> configClass : configClasses) {
            try {
                Class.forName(configClass.getName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to load configuration class: " + configClass.getName(), e);
            }
        }
    }
}

