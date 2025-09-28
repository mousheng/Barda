package com.barda.api.framework.filter;

import static com.barda.api.framework.filter.FilterOrder.REQUEST_COST;

import java.time.Duration;

import javax.annotation.Nonnull;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 一个WebFilter实现类，用于计算和记录HTTP请求的处理成本。
 * 该类使用了Slf4j来进行日志记录，并实现了WebFilter和Ordered接口。
 * 该类在WebFilterChain中执行的顺序由getOrder()方法返回的值确定。
 */
@Slf4j
@Component
public class RequestCostFilter implements WebFilter, Ordered {

    /**
     * 重写filter()方法，用于在HTTP请求处理之前记录开始时间。
     * 在HTTP响应提交之前，在HTTP头中添加X-REQUEST-COST字段，用于记录处理成本。
     *
     * @param exchange  ServerWebExchange，表示一个HTTP请求和响应
     * @param chain     WebFilterChain，表示一个WebFilter的链
     * @return  一个Mono<Void>，表示处理完成
     */
    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        long currentTime = System.nanoTime();
        exchange.getResponse().beforeCommit(() -> Mono.fromRunnable(() -> {
            HttpHeaders httpHeaders = exchange.getResponse().getHeaders();
            httpHeaders.add("X-REQUEST-COST", Duration.ofNanos(System.nanoTime() - currentTime).toMillis() + "ms");
        }));
        return chain.filter(exchange);
    }

    /**
     * 重写getOrder()方法，用于确定该WebFilter在WebFilterChain中的执行顺序。
     * 值越小，执行的优先级越高。
     *
     * @return  该WebFilter在WebFilterChain中的执行顺序
     */
    @Override
    public int getOrder() {
        return REQUEST_COST.getOrder();
    }
}
