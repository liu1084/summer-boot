package org.summer.boot.interceptor;

import org.summer.boot.web.Request;
import org.summer.boot.web.Response;

import java.util.List;

public class InterceptorChain {
    private final List<InterceptorInterface> interceptors;
    private int currentIndex = -1;

    public InterceptorChain(List<InterceptorInterface> interceptors) {
        this.interceptors = interceptors;
    }

    public void proceed(Request request, Response response) {
        currentIndex++;
        if (currentIndex < interceptors.size()) {
            interceptors.get(currentIndex).intercept(request, response, this);
        }
    }
}

