package com.barda.api.framework.filter;

import static com.barda.api.framework.filter.Position.AFTER_PROXY_CHAIN;
import static com.barda.api.framework.filter.Position.BEFORE_PROXY_CHAIN;

import org.springframework.core.PriorityOrdered;

/**
 * 该枚举类定义了过滤器的执行顺序，并为每个过滤器指定了在代理链中的执行位置。
 * 它使用了Java的枚举类型来定义一组相关的常量，并为每个常量添加了相关的属性和方法。
 */
public enum FilterOrder {

    /**
     * 请求成本（在代理链之前）
     */
    REQUEST_COST(BEFORE_PROXY_CHAIN),
    /**
     * 限流（在代理链之前）
     */
    THROTTLING(BEFORE_PROXY_CHAIN),

    // 在此处添加WEB_FILTER_CHAIN_PROXY

    /**
     * 用户封禁（在代理链之后）
     */
    USER_BAN(AFTER_PROXY_CHAIN),
    /**
     * 全局上下文（在代理链之后）- 当前组织成员在此处设置，需要当前组织成员的过滤器应放置在此过滤器之后
     */
    GLOBAL_CONTEXT(AFTER_PROXY_CHAIN),
    /**
     * 查询执行 HTTP 请求主体大小（在代理链之后）
     */
    QUERY_EXECUTE_HTTP_BODY_SIZE(AFTER_PROXY_CHAIN),
    /**
     * 定价特性（在代理链之后）
     */
    PRICING_FEATURE(AFTER_PROXY_CHAIN),
    ;


    /**
     * 每个枚举值之间的顺序间隔
     */
    private static final int INTERVAL = 100;
    /**
     * 表示过滤器在代理链中的位置的枚举值
     */
    private final Position positionToProxyChain;

    /**
     * 构造函数，用于初始化FilterOrder枚举常量。
     *
     * @param positionToProxyChain 该常量在代理链中的执行位置
     */
    FilterOrder(Position positionToProxyChain) {
        this.positionToProxyChain = positionToProxyChain;
    }

    /**
     * 获取该过滤器在代理链中的执行顺序。
     *
     * @return 该过滤器在代理链中的执行顺序
     */
    public int getOrder() {
        if (positionToProxyChain == BEFORE_PROXY_CHAIN) {
            // 由于序号是从零开始的，所以这里我们从PriorityOrdered.HIGHEST_PRECEDENCE + INTERVAL开始
            return (PriorityOrdered.HIGHEST_PRECEDENCE + INTERVAL) + ordinal() * INTERVAL;
        }
        return ordinal() * INTERVAL; // WEB_FILTER_CHAIN_FILTER_ORDER = 0 - 100; 所以这里我们从零开始，并添加一个偏移量=ordinal * INTERVAL
    }
}

/**
 * 代理链中的身份验证，需要用户身份验证的过滤器应该放置在这个代理过滤器之后
 */
enum Position {
    /** 在代理链之前 */
    BEFORE_PROXY_CHAIN,
    /** 在代理链之后 */
    AFTER_PROXY_CHAIN,
}

