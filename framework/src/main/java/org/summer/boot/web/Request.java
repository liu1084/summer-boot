package org.summer.boot.web;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;
import lombok.Getter;

@Getter
public class Request {
    private final FullHttpRequest fullHttpRequest;

    public Request(FullHttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
    }

    public String getUri() {
        return fullHttpRequest.uri();
    }

    public HttpMethod getMethod() {
        return fullHttpRequest.method();
    }

    public String getMethodName() {
        return this.getMethod().name();
    }

    public HttpHeaders getHeaders() {
        // 返回头信息的Map
        return fullHttpRequest.headers();
    }

    public String getBody() {
        return fullHttpRequest.content().toString(CharsetUtil.UTF_8);
    }
}
