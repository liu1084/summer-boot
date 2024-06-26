package org.summer.boot.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Server {
    private int port = 8080;
    private int bossGroupThreads;
    private int workerGroupThreads;
    private int maxContentLength;
    private int threadPool = 10;
}
