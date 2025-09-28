package com.barda.api.framework.filter;

import static com.barda.api.framework.filter.FilterOrder.THROTTLING;
import static com.barda.sdk.exception.BizError.REQUEST_THROTTLED;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.google.common.util.concurrent.RateLimiter;
import com.barda.sdk.config.dynamic.ConfigCenter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


/**
 * 一个WebFilter实现类，用于对HTTP请求进行限流。
 * 该类使用了Slf4j来进行日志记录，并实现了WebFilter和Ordered接口。
 * 该类在WebFilterChain中执行的顺序由getOrder()方法返回的值确定。
 * 该类使用了RateLimiter来实现限流功能，并使用了ConfigCenter来获取配置的限流阈值。
 */
@SuppressWarnings("UnstableApiUsage")
@Slf4j
@Component
public class ThrottlingFilter implements WebFilter, Ordered {

    private static final int DEFAULT_RATE_THRESHOLD = 50;

    /**
     * 用于存储RateLimiterWrapper的Map
     */
    private final Map<String, RateLimiterWrapper> rateLimiterMap = new ConcurrentHashMap<>();

    /**
     * 一个Supplier，用于提供URL和限流阈值的映射
     */
    private Supplier<Map<String, Integer>> urlRateLimiter;

    /**
     * ConfigCenter的实例，用于获取配置的限流阈值
     */
    @Autowired
    private ConfigCenter configCenter;

    /**
     * 初始化方法，用于从ConfigCenter中获取限流阈值
     */
    @PostConstruct
    private void init() {
        urlRateLimiter = configCenter.threshold().ofMap("urlRateLimiter", String.class, Integer.class, emptyMap());
    }

    /**
     * 重写filter()方法，用于对HTTP请求进行限流
     *
     * @param exchange  ServerWebExchange，表示一个HTTP请求和响应
     * @param chain     WebFilterChain，表示一个WebFilter的链
     * @return  一个Mono<Void>，表示处理完成
     */
    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String requestUrl = request.getPath().pathWithinApplication().value();

        RateLimiterWrapper rateLimiter = rateLimiterMap.compute(requestUrl,
                (url, currentLimiter) -> {
                    int targetRate = urlRateLimiter.get().getOrDefault(url, DEFAULT_RATE_THRESHOLD);
                    if (currentLimiter == null) {
                        return RateLimiterWrapper.create(targetRate);
                    }
                    if (currentLimiter.rateNotChanged(targetRate)) {
                        return currentLimiter;
                    }
                    return currentLimiter.updateRate(targetRate);
                });

        if (!rateLimiter.tryAcquire()) {
            return ofError(REQUEST_THROTTLED, "REQUEST_THROTTLED");
        }

        return chain.filter(exchange);
    }

    /**
     * 重写getOrder()方法，用于确定该WebFilter在WebFilterChain中的执行顺序
     * 值越小，执行的优先级越高
     *
     * @return  该WebFilter在WebFilterChain中的执行顺序
     */
    @Override
    public int getOrder() {
        return THROTTLING.getOrder();
    }

    /**
     * 一个封装了RateLimiter的类
     */
    @Getter
    private static class RateLimiterWrapper {
        /**
         * 一个RateLimiter
         */
        private final RateLimiter rateLimiter;

        /**
         * 记录RateLimiterWrapper被创建的时间
         */
        private volatile long initMillis;

        /**
         * 私有构造函数，用于创建RateLimiterWrapper
         *
         * @param targetRate  目标限流速率
         */
        private RateLimiterWrapper(int targetRate) {
            this.rateLimiter = RateLimiter.create(targetRate);
            this.initMillis = System.currentTimeMillis();
        }

        /**
         * 静态方法，用于创建RateLimiterWrapper
         *
         * @param targetRate  目标限流速率
         * @return  一个RateLimiterWrapper
         */
        public static RateLimiterWrapper create(int targetRate) {
            return new RateLimiterWrapper(targetRate);
        }

        /**
         * 判断限流速率是否未发生变化
         *
         * @param targetRate  目标限流速率
         * @return  true表示未发生变化，false表示发生了变化
         */
        public boolean rateNotChanged(int targetRate) {
            return Math.abs(targetRate - rateLimiter.getRate()) < 1e-6;
        }

        /**
         * 更新RateLimiter的限流速率
         *
         * @param targetRate  目标限流速率
         * @return  一个RateLimiterWrapper
         */
        public RateLimiterWrapper updateRate(int targetRate) {
            rateLimiter.setRate(targetRate);
            initMillis = System.currentTimeMillis();
            return this;
        }

        /**
         * 尝试获取令牌
         *
         * @return  true表示获取到了令牌，false表示未获取到令牌
         */
        public boolean tryAcquire() {
            // 可能在刚刚初始化时就失败，所以这里给了1秒的缓冲时间
            if (System.currentTimeMillis() - initMillis <= 1000) {
                return true;
            }
            return rateLimiter.tryAcquire();
        }

    }

}
