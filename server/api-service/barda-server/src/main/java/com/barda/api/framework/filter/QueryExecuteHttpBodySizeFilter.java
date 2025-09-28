package com.barda.api.framework.filter;

import static com.barda.api.framework.filter.FilterOrder.QUERY_EXECUTE_HTTP_BODY_SIZE;
import static com.barda.sdk.exception.BizError.EXCEED_QUERY_REQUEST_SIZE;
import static com.barda.sdk.exception.BizError.EXCEED_QUERY_RESPONSE_SIZE;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import jakarta.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.config.dynamic.ConfigInstanceHelper;
import com.barda.sdk.exception.BizException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 重写WebFilter的filter方法，用于在请求处理之前执行过滤操作。
 * 该方法获取访客ID并记录到日志中，然后构建全局上下文并存储在ServerWebExchange的上下文中。
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "common.cloud", havingValue = "true")
public class QueryExecuteHttpBodySizeFilter implements WebFilter, Ordered {

    /**
     * 用于获取配置中心的配置的服务
     */
    @Autowired
    private ConfigCenter configCenter;

    /**
     * 用于获取通用配置的配置
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 用于帮助获取配置实例的辅助类
     */
    private ConfigInstanceHelper configInstance;

    /**
     * 初始化方法，在该类被实例化时执行
     */
    @PostConstruct
    public void init() {
        configInstance = new ConfigInstanceHelper(configCenter.threshold());
    }

    /**
     * 重写WebFilter的filter方法，用于在请求处理之前执行过滤操作。
     * 该方法检查请求路径是否为查询API的路径，如果是，则检查请求和响应的大小是否超出了配置的阈值。
     * 如果超出了阈值，则返回400 Bad Request的响应。
     *
     * @param exchange ServerWebExchange，表示一个HTTP请求和响应
     * @param chain    WebFilterChain，表示一个WebFilter的链
     * @return Mono<Void>，表示一个空的Mono，表示处理完成
     */
    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        // 检查查询API
        if (path.startsWith(NewUrl.QUERY_URL) || path.startsWith(Url.QUERY_URL)) {

            long maxRequestSize = configInstance.ofLong("maxRequestSize",
                    commonConfig.getMaxQueryRequestSizeInMb() * FileUtils.ONE_MB);
            long maxResponseSize = configInstance.ofLong("maxResponseSize",
                    commonConfig.getMaxQueryResponseSizeInMb() * FileUtils.ONE_MB);

            ServerWebExchange newServerWebExchange = exchange.mutate()
                    .request(new CustomServerHttpRequestDecorator(exchange.getRequest(), maxRequestSize))
                    .response(new CustomServerHttpResponseDecorator(exchange.getResponse(), maxResponseSize))
                    .build();
            return chain.filter(newServerWebExchange);
        }
        // 直接通过
        return chain.filter(exchange);
    }

    /**
     * 获取过滤器的执行顺序。
     * 该方法返回一个整数，用于确定过滤器在WebFilterChain中的执行顺序。
     * 值越小，执行的优先级越高。
     *
     * @return 过滤器的执行顺序
     */
    @Override
    public int getOrder() {
        return QUERY_EXECUTE_HTTP_BODY_SIZE.getOrder();
    }

    /**
     * 自定义的ServerHttpRequestDecorator类，用于检查查询请求的大小。
     * 该类继承自ServerHttpRequestDecorator，并重写了getBody()方法。
     * 在getBody()方法中，检查读取的字节数是否超出了配置的阈值。
     * 如果超出了阈值，则返回一个Mono.error()，并抛出一个BizException。
     */
    private static class CustomServerHttpRequestDecorator extends ServerHttpRequestDecorator {

        /**
         * 用于记录读取的字节数的原子变量
         */
        private final AtomicLong readBytes = new AtomicLong();

        /**
         * 配置的最大读取字节数
         */
        private final long maxReadBytes;

        /**
         * 构造函数，用于初始化父类和maxReadBytes
         *
         * @param delegate  被装饰的ServerHttpRequest
         * @param maxReadBytes  配置的最大读取字节数
         */
        public CustomServerHttpRequestDecorator(ServerHttpRequest delegate, long maxReadBytes) {
            super(delegate);
            this.maxReadBytes = maxReadBytes;
        }

        /**
         * 重写getBody()方法，用于检查读取的字节数是否超出了配置的阈值
         *
         * @return  一个Flux<DataBuffer>，用于读取请求的正文
         */
        @Nonnull
        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody()
                    .delayUntil(dataBuffer -> {
                        if (readBytes.addAndGet(dataBuffer.readableByteCount()) > maxReadBytes) {
                            return Mono.error(new BizException(EXCEED_QUERY_REQUEST_SIZE, "EXCEED_QUERY_REQUEST_SIZE"));
                        }
                        return Mono.empty();
                    });
        }
    }

    /**
     * 自定义的ServerHttpResponseDecorator类，用于检查查询响应的大小。
     * 该类继承自ServerHttpResponseDecorator，并重写了writeWith()方法。
     * 在writeWith()方法中，检查写入的字节数是否超出了配置的阈值。
     * 如果超出了阈值，则返回一个Mono.error()，并抛出一个BizException。
     */
    private static class CustomServerHttpResponseDecorator extends ServerHttpResponseDecorator {

        /**
         * 用于记录写入的字节数的原子变量
         */
        private final AtomicLong writeBytes = new AtomicLong();

        /**
         * 配置的最大写入字节数
         */
        private final long maxWriteBytes;

        /**
         * 构造函数，用于初始化父类和maxWriteBytes
         *
         * @param delegate  被装饰的ServerHttpResponse
         * @param maxWriteBytes  配置的最大写入字节数
         */
        public CustomServerHttpResponseDecorator(ServerHttpResponse delegate, long maxWriteBytes) {
            super(delegate);
            this.maxWriteBytes = maxWriteBytes;
        }

        /**
         * 重写writeWith()方法，用于检查写入的字节数是否超出了配置的阈值
         *
         * @param body  一个Publisher<? extends DataBuffer>，用于写入响应的正文
         * @return  一个Mono<Void>，表示写入操作完成
         */
        @Nonnull
        @Override
        public Mono<Void> writeWith(@Nonnull Publisher<? extends DataBuffer> body) {
            return super.writeWith(Flux.from(body)
                    .delayUntil(dataBuffer -> {
                        if (writeBytes.addAndGet(dataBuffer.readableByteCount()) > maxWriteBytes) {
                            return Mono.error(new BizException(EXCEED_QUERY_RESPONSE_SIZE, "EXCEED_QUERY_RESPONSE_SIZE"));
                        }
                        return Mono.empty();
                    }));
        }
    }
}
