package com.barda.api.framework.filter;

import static com.barda.api.framework.filter.FilterOrder.GLOBAL_CONTEXT;
import static com.barda.sdk.constants.Authentication.isAnonymousUser;
import static com.barda.sdk.constants.GlobalContext.CLIENT_IP;
import static com.barda.sdk.constants.GlobalContext.CLIENT_LOCALE;
import static com.barda.sdk.constants.GlobalContext.CURRENT_ORG_MEMBER;
import static com.barda.sdk.constants.GlobalContext.DOMAIN;
import static com.barda.sdk.constants.GlobalContext.REQUEST;
import static com.barda.sdk.constants.GlobalContext.REQUEST_ID_LOG;
import static com.barda.sdk.constants.GlobalContext.REQUEST_METHOD;
import static com.barda.sdk.constants.GlobalContext.REQUEST_PATH;
import static com.barda.sdk.constants.GlobalContext.VISITOR_ID;
import static com.barda.sdk.constants.GlobalContext.VISITOR_TOKEN;
import static com.barda.sdk.util.IDUtils.generate;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.barda.api.framework.service.GlobalContextService;
import com.barda.api.home.SessionUserService;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.infra.serverlog.ServerLog;
import com.barda.infra.serverlog.ServerLogService;
import com.barda.infra.util.NetworkUtils;
import com.barda.sdk.util.CookieHelper;
import com.barda.sdk.util.UriUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 全局上下文过滤器，用于在请求处理之前注入全局上下文信息。
 * 包括访客ID、客户端IP、请求ID、请求路径、请求方法、客户端语言、
 * 当前组织成员、访客令牌、来源域名等。
 * 并将这些信息记录到MDC中，方便在日志中使用。
 * 同时，将全局上下文信息存储在ServerWebExchange的上下文中，
 * 方便在后续的处理器中使用。
 */
@Component
@Slf4j
public class GlobalContextFilter implements WebFilter, Ordered {

    /**
     * 用于从请求头中提取MDC信息的前缀
     */
    private static final String MDC_HEADER_PREFIX = "X-MDC-";

    /**
     * 用于在请求头中存储请求ID的键
     */
    private static final String REQUEST_ID_HEADER = "X-REQUEST-ID";

    /**
     * 存储在ServerWebExchange的上下文中的全局上下文键
     */
    public static final String CONTEXT_MAP = "context-map";

    /**
     * 用于获取访客ID的服务
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 用于获取当前组织成员的服务
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 用于记录服务器日志的服务
     */
    @Autowired
    private ServerLogService serverLogService;

    /**
     * 用于获取客户端语言的服务
     */
    @Autowired
    private GlobalContextService globalContextService;

    /**
     * 用于获取访客令牌的帮助类
     */
    @Autowired
    private CookieHelper cookieHelper;

    /**
 * 重写WebFilter的filter方法，用于在请求处理之前执行过滤操作。
 * 该方法获取访客ID并记录到日志中，然后构建全局上下文并存储在ServerWebExchange的上下文中。
 *
 * @param exchange ServerWebExchange，表示一个HTTP请求和响应
 * @param chain    WebFilterChain，表示一个WebFilter的链
 * @return Mono<Void>，表示一个空的Mono，表示处理完成
 */
    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {

        // 获取访客ID并记录到日志中
        return sessionUserService.getVisitorId()
                .doOnNext(visitorId -> {
                    if (isAnonymousUser(visitorId)) {
                        return;
                    }
                    ServerLog serverLog = ServerLog.builder()
                            .userId(visitorId)
                            .urlPath(exchange.getRequest().getPath().toString())
                            .httpMethod(Optional.ofNullable(exchange.getRequest().getMethod()).map(HttpMethod::name).orElse(""))
                            .createTime(System.currentTimeMillis())
                            .build();
                    serverLogService.record(serverLog);
                })
                // 构建全局上下文并存储在ServerWebExchange的上下文中
                .flatMap(visitorId -> chain.filter(exchange)
                        .contextWrite(ctx -> {
                            Map<String, Object> contextMap = buildContextMap(exchange, visitorId);
                            for (Entry<String, Object> entry : contextMap.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();
                                ctx = ctx.put(key, value);
                            }
                            return ctx.put(CONTEXT_MAP, contextMap);
                        }));
    }

    /**
     * 构建全局上下文信息
     *
     * @param serverWebExchange ServerWebExchange
     * @param visitorId         访客ID
     * @return 全局上下文信息
     */
    private Map<String, Object> buildContextMap(ServerWebExchange serverWebExchange, String visitorId) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        Map<String, Object> contextMap = request.getHeaders().toSingleValueMap().entrySet()
                .stream()
                .filter(x -> x.getKey().startsWith(MDC_HEADER_PREFIX))
                .collect(toMap(v -> v.getKey().substring((MDC_HEADER_PREFIX.length())), Map.Entry::getValue));
        contextMap.put(VISITOR_ID, visitorId);
        contextMap.put(CLIENT_IP, NetworkUtils.getRemoteIp(serverWebExchange));
        contextMap.put(REQUEST_ID_LOG, getOrCreateRequestId(request));
        contextMap.put(REQUEST_PATH, request.getPath().pathWithinApplication().value());
        contextMap.put(REQUEST, request);
        contextMap.put(REQUEST_METHOD, ofNullable(request.getMethod()).map(HttpMethod::name).orElse(""));
        contextMap.put(CLIENT_LOCALE, globalContextService.getClientLocale(request));
        contextMap.put(CURRENT_ORG_MEMBER, orgMemberService.getCurrentOrgMember(visitorId).cache());
        contextMap.put(VISITOR_TOKEN, cookieHelper.getCookieToken(serverWebExchange));
        contextMap.put(DOMAIN, UriUtils.getRefererDomainFromRequest(serverWebExchange));
        return contextMap;
    }

    /**
     * 获取或创建请求ID
     *
     * @param request ServerHttpRequest
     * @return 请求ID
     */
    @SuppressWarnings("ConstantConditions")
    private String getOrCreateRequestId(final ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(REQUEST_ID_HEADER)) {
            request.mutate().header(REQUEST_ID_HEADER, generate()).build();
        }

        return request.getHeaders().get(REQUEST_ID_HEADER).get(0);
    }

    @Override
    public int getOrder() {
        return GLOBAL_CONTEXT.getOrder();
    }
}