package org.summer.boot;

import org.summer.boot.annotations.WebApplication;
import org.summer.boot.web.NettyServer;

@WebApplication
public class Application {
    public static void main(String[] args) throws Exception {
        NettyServer.run(Application.class, args);
    }
}
