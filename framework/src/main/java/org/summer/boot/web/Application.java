package org.summer.boot.web;

import org.summer.boot.annotations.WebApplication;

@WebApplication
public class Application {
    public static void main(String[] args) throws Exception {
        NettyServer.run(Application.class, args);
    }
}
