package org.summer.boot.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.reflections.Reflections;
import org.summer.boot.annotations.*;
import org.summer.boot.config.AutoConfigurationLoader;
import org.summer.boot.config.ConfigBinder;
import org.summer.boot.config.ServerProperties;
import org.summer.boot.filter.FilterInterface;
import org.summer.boot.filter.FilterManager;
import org.summer.boot.inject.ApplicationModule;
import org.summer.boot.interceptor.InterceptorInterface;
import org.summer.boot.interceptor.InterceptorManager;
import org.summer.boot.plugin.PluginInterface;
import org.summer.boot.plugin.PluginManager;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class NettyServer {
    private final PluginManager pluginManager;
    private final InterceptorManager interceptorManager;
    private final FilterManager filterManager;
    static Injector injector;
    static String[] scanBasePackages;
    private int port;

    public NettyServer(PluginManager pluginManager, InterceptorManager interceptorManager, FilterManager filterManager) {
        this.pluginManager = pluginManager;
        this.interceptorManager = interceptorManager;
        this.filterManager = filterManager;
    }

    public void start() throws Exception {
        // 加载自动配置
        AutoConfigurationLoader.loadConfigurations();
        // 扫描并注册插件、拦截器和过滤器
        scanAndRegisterComponents(Plugin.class, PluginInterface.class, pluginManager::registerPlugin);
        scanAndRegisterComponents(Interceptor.class, InterceptorInterface.class, interceptorManager::addInterceptor);
        scanAndRegisterComponents(Filter.class, FilterInterface.class, filterManager::addFilter);

        Injector injector = Guice.createInjector(new ApplicationModule(scanBasePackages));

        // 初始化路由处理器和路由扫描器
        RouteHandler routeHandler = new RouteHandler(injector, scanBasePackages);

        // 启动Netty服务器
        ServerBootstrap bootstrap = new ServerBootstrap();
        ServerProperties serverProperties = injector.getInstance(ServerProperties.class);

        int bossThreads = serverProperties.getBossGroupThreads();
        int workerThreads = serverProperties.getWorkerGroupThreads();
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreads);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads);

        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new HttpObjectAggregator(serverProperties.getMaxContentLength()));
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
                            FullHttpResponse response = routeHandler.handleRequest(request);
                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                        }
                    });
                }
            });
            port = serverProperties.getPort();
            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private <A extends Annotation, T> void scanAndRegisterComponents(Class<A> annotation, Class<T> targetType, Consumer<T> registryFunction) {
        for (String basePackage : scanBasePackages) {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation);

            List<Class<?>> sortedClasses = new ArrayList<>(annotatedClasses);
            sortedClasses.sort(Comparator.comparingInt(clazz -> {
                Order order = clazz.getAnnotation(Order.class);
                return (order != null) ? order.value() : Integer.MAX_VALUE;
            }));

            for (Class<?> clazz : sortedClasses) {
                T instance = targetType.cast(injector.getInstance(clazz));
                registryFunction.accept(instance);
            }
        }
    }


    public static void run(Class<?> mainClass, String[] args) throws Exception {
        String[] basePackage = {mainClass.getPackage().getName()};
        WebApplication webApplication = mainClass.getAnnotation(WebApplication.class);
        if (webApplication != null) {
            scanBasePackages = webApplication.scanBasePackages();
        } else {
            scanBasePackages = basePackage;
        }

        //merge
        scanBasePackages = Stream.concat(Stream.of(scanBasePackages), Stream.of(basePackage)).toArray(String[]::new);
        // Initialize ConfigBinder and bind configurations
        ConfigBinder configBinder = new ConfigBinder();
        configBinder.bindConfigurations(scanBasePackages);

        PluginManager pluginManager = new PluginManager();
        InterceptorManager interceptorManager = new InterceptorManager();
        FilterManager filterManager = new FilterManager();

        NettyServer nettyServer = new NettyServer(pluginManager, interceptorManager, filterManager);
        nettyServer.start();
    }
}

