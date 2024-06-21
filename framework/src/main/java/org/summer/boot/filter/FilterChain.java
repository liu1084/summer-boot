package org.summer.boot.filter;

import org.summer.boot.web.Request;
import org.summer.boot.web.Response;

import java.util.List;

public class FilterChain {
    private final List<FilterInterface> filters;
    private int currentIndex = -1;

    public FilterChain(List<FilterInterface> filters) {
        this.filters = filters;
    }

    public void doFilter(Request request, Response response) {
        currentIndex++;
        if (currentIndex < filters.size()) {
            filters.get(currentIndex).filter(request, response, this);
        }
    }
}

