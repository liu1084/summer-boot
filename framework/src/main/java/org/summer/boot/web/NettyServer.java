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
import org.summer.boot.config.ConfigurationManager;
import org.summer.boot.config.ServerProperties;
import org.summer.boot.constants.Constant;
import org.summer.boot.filter.FilterInterface;
import org.summer.boot.filter.FilterManager;
import org.summer.boot.inject.ApplicationModule;
import org.summer.boot.interceptor.InterceptorInterface;
import org.summer.boot.interceptor.InterceptorManager;
import org.summer.boot.plugin.PluginInterface;
import org.summer.boot.plugin.PluginManager;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class NettyServer {
    private final PluginManager pluginManager;
    private final InterceptorManager interceptorManager;
    private final FilterManager filterManager;
    private static Injector injector;
    private static Set<String> basePackages;
    private int port;


    public NettyServer(PluginManager pluginManager, InterceptorManager interceptorManager, FilterManager filterManager) {
        this.pluginManager = pluginManager;
        this.interceptorManager = interceptorManager;
        this.filterManager = filterManager;
    }

    public void start() throws Exception {
        // 扫描并注册插件、拦截器和过滤器
        scanAndRegisterComponents(Plugin.class, PluginInterface.class, pluginManager::registerPlugin);
        scanAndRegisterComponents(Interceptor.class, InterceptorInterface.class, interceptorManager::addInterceptor);
        scanAndRegisterComponents(Filter.class, FilterInterface.class, filterManager::addFilter);

        // 初始化路由处理器和路由扫描器
        RouteHandler routeHandler = new RouteHandler(injector, basePackages);

        // 启动Netty服务器
        ServerBootstrap bootstrap = new ServerBootstrap();
        ServerProperties serverProperties = injector.getInstance(ServerProperties.class);

        int bossThreads = serverProperties.getServer().getBossGroupThreads();
        int workerThreads = serverProperties.getServer().getWorkerGroupThreads();
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreads);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads);

        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new HttpObjectAggregator(serverProperties.getServer().getMaxContentLength()));
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
                            FullHttpResponse response = routeHandler.handleRequest(request);
                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                        }
                    });
                }
            });
            port = serverProperties.getServer().getPort();
            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private <A extends Annotation, T> void scanAndRegisterComponents(Class<A> annotation, Class<T> targetType, Consumer<T> registryFunction) {
        basePackages.parallelStream().forEach(basePackage -> {
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
        });
    }


    public static void run(Class<?> mainClass, String[] args) throws Exception {
        basePackages = new HashSet<>(Arrays.asList(Constant.BASE_PACKAGES));
        String mainClassPackage = mainClass.getPackage().getName();
        basePackages.add(mainClassPackage);

        WebApplication webApplication = mainClass.getAnnotation(WebApplication.class);
        if (webApplication != null) {
            basePackages.addAll(Arrays.asList(webApplication.getScanBasePackages()));
        }

        injector = Guice.createInjector(new ApplicationModule(basePackages));

        // 初始化服务配置
        ConfigurationManager.loadProperties(mainClass);
        // 加载自动配置的类
        AutoConfigurationLoader autoConfigurationLoader = new AutoConfigurationLoader(basePackages, injector);
        autoConfigurationLoader.loadAutoConfigurations();
        // 插件管理器
        PluginManager pluginManager = new PluginManager();
        // 拦截器管理器
        InterceptorManager interceptorManager = new InterceptorManager();
        // 过滤器管理器
        FilterManager filterManager = new FilterManager();

        NettyServer nettyServer = new NettyServer(pluginManager, interceptorManager, filterManager);
        nettyServer.start();
    }
}

