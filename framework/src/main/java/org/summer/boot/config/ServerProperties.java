package org.summer.boot.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.summer.boot.annotations.ConfigurationProperties;
import org.summer.boot.annotations.EnableAutoConfiguration;

@ConfigurationProperties(prefix = "server")
@Getter
@Setter
public class ServerProperties {
    @JsonProperty("env")
    private Environment environment;

    @JsonProperty("server")
    private Server server;

    @JsonProperty("isDebug")
    private boolean isDebug = false;
}
