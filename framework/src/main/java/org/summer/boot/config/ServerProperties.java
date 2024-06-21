package org.summer.boot.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.summer.boot.annotations.ConfigurationProperties;

@ConfigurationProperties(prefix = "server")
@Getter
@Setter
public class ServerProperties {

    @JsonProperty("port")
    private int port = 8080; // 默认端口

    @JsonProperty("bossGroupThreads")
    private int bossGroupThreads = 1; // 默认线程数

    @JsonProperty("workerGroupThreads")
    private int workerGroupThreads = 2; // 默认线程数

    @JsonProperty("maxContentLength")
    private int maxContentLength = 65536; // 默认最大内容长度
}
