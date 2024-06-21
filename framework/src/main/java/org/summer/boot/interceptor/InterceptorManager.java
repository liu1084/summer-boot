package org.summer.boot.interceptor;

import org.summer.boot.web.Request;
import org.summer.boot.web.Response;

import java.util.ArrayList;
import java.util.List;

public class InterceptorManager {
    private final List<InterceptorInterface> interceptors = new ArrayList<>();

    public void addInterceptor(InterceptorInterface interceptor) {
        interceptors.add(interceptor);
    }

    public void applyInterceptors(Request request, Response response) {
        // 应用拦截器逻辑
        InterceptorChain chain = new InterceptorChain(interceptors);
        chain.proceed(request, response);
    }
}


