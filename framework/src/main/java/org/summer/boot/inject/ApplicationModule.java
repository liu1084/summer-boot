package org.summer.boot.inject;

import com.google.inject.AbstractModule;
import org.reflections.Reflections;
import org.summer.boot.annotations.*;
import org.summer.boot.config.ConfigurationManager;
import org.summer.boot.config.ServerProperties;

import java.lang.annotation.Annotation;
import java.util.Set;

public class ApplicationModule extends AbstractModule {

    private final Set<String> basePackages;

    public ApplicationModule(Set<String> basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    protected void configure() {
        // 绑定配置类
        bind(ServerProperties.class).toProvider(() -> ConfigurationManager.getConfiguration(ServerProperties.class));

        // 扫描并绑定 Service, Repository 和 Component 注解的类
        bindAnnotatedClasses(Service.class);
        bindAnnotatedClasses(Repository.class);
        bindAnnotatedClasses(Component.class);
        bindAnnotatedClasses(Plugin.class);
        bindAnnotatedClasses(Interceptor.class);
        bindAnnotatedClasses(Filter.class);
    }

    private <T extends Annotation> void bindAnnotatedClasses(Class<T> annotation) {
        for (String basePackage : basePackages) {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation);
            for (Class<?> clazz : annotatedClasses) {
                bind(clazz);
            }
        }
    }
}


