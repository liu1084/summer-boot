package org.summer.boot.interceptor;

import org.summer.boot.web.Request;
import org.summer.boot.web.Response;

public interface InterceptorInterface {
    void intercept(Request request, Response response, InterceptorChain chain);
    boolean preHandle(Request request, Response response);
    void postHandle(Request request, Response response);
}
