package org.summer.boot.web;

import com.google.inject.Injector;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reflections.Reflections;
import org.summer.boot.annotations.Controller;
import org.summer.boot.annotations.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class RouteHandler {

    private final Map<String, Method> routeMap = new HashMap<>();
    private final Map<String, Object> controllerInstances = new HashMap<>();

    private final Injector injector;

    public RouteHandler(Injector injector, Set<String> basePackages) {
        this.injector = injector;
        scanForControllers(basePackages);
    }

    private void scanForControllers(Set<String> basePackages) {
        basePackages.parallelStream().distinct().forEach(basePackage -> {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

            for (Class<?> controllerClass : controllerClasses) {
                Object controllerInstance = injector.getInstance(controllerClass);
                controllerInstances.put(controllerClass.getName(), controllerInstance);

                for (Method method : controllerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        routeMap.put(requestMapping.value(), method);
                    }
                }
            }
        });
    }

    public FullHttpResponse handleRequest(FullHttpRequest request) {
        String uri = request.uri();
        Method method = routeMap.get(uri);

        if (method == null) {
            return createNotFoundResponse();
        }

        Object controllerInstance = controllerInstances.get(method.getDeclaringClass().getName());
        try {
            Object response = method.invoke(controllerInstance, request);
            return (FullHttpResponse) response;
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(e);
        }
    }

    private FullHttpResponse createNotFoundResponse() {
        // 返回404响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        return response;
    }

    private FullHttpResponse createErrorResponse(Exception e) {
        // 返回500响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        return response;
    }
}




