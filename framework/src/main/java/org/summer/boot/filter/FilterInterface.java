package org.summer.boot.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.summer.boot.web.Request;
import org.summer.boot.web.Response;

public interface FilterInterface {
    void filter(Request request, Response response, FilterChain chain);
}
