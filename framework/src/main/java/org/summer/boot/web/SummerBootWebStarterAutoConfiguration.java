package org.summer.boot.web;

import com.google.inject.AbstractModule;
import org.summer.boot.annotations.EnableAutoConfiguration;
import org.summer.boot.config.ConfigurationManager;
import org.summer.boot.config.ServerProperties;

@EnableAutoConfiguration
public class SummerBootWebStarterAutoConfiguration extends AbstractModule {

    @Override
    protected void configure() {
        bind(ServerProperties.class).toProvider(() -> ConfigurationManager.getConfiguration(ServerProperties.class));
    }
}
