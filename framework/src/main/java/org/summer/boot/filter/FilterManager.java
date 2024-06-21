package org.summer.boot.filter;

import org.summer.boot.web.Request;
import org.summer.boot.web.Response;

import java.util.ArrayList;
import java.util.List;

public class FilterManager {
    private final List<FilterInterface> filters = new ArrayList<>();

    public void addFilter(FilterInterface filter) {
        filters.add(filter);
    }

    public void applyFilters(Request request, Response response) {
        // 应用过滤器逻辑
        FilterChain chain = new FilterChain(filters);
        chain.doFilter(request, response);
    }
}

