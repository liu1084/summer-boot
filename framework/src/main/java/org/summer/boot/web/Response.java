package org.summer.boot.web;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.Getter;

@Getter
public class Response {
    private final FullHttpResponse fullHttpResponse;

    public Response(FullHttpResponse fullHttpResponse) {
        this.fullHttpResponse = fullHttpResponse;
    }

    public void setStatus(HttpResponseStatus status) {
        fullHttpResponse.setStatus(status);
    }

    public void setContent(String content) {
        fullHttpResponse.content().writeBytes(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
    }

    public void addHeader(String name, String value) {
        fullHttpResponse.headers().set(name, value);
    }
}
