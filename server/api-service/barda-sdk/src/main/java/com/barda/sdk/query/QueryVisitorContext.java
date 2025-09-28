package com.barda.sdk.query;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpCookie;
import org.springframework.util.MultiValueMap;

import com.barda.sdk.models.Property;

import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * 查询访问者上下文类。
 * 该类封装了查询访问者的相关信息，并提供了一组 getter 方法来访问这些信息。
 */
@Getter
public class QueryVisitorContext {

    /**
     * 访问者 ID。
     */
    private final String visitorId;

    /**
     * 应用组织 ID。
     */
    private final String applicationOrgId;
    /**
     * HTTP cookies。
     */
    private final MultiValueMap<String, HttpCookie> cookies;

    /**
     * 系统端口。
     */
    private final int systemPort;

    /**
     * 鉴权令牌的 Mono 对象。
     */
    private final Mono<List<Property>> authTokenMono;

    /**
     * 被禁止的主机集合。
     */
    private final Set<String> disallowedHosts;

    /**
     * 构造函数。
     *
     * @param visitorId  访问者 ID。
     * @param applicationOrgId  应用组织 ID。
     * @param systemPort  系统端口。
     * @param cookies  HTTP cookies。
     * @param authTokenMono  鉴权令牌的 Mono 对象。
     * @param disallowedHosts  被禁止的主机集合。
     */
    public QueryVisitorContext(String visitorId, String applicationOrgId, int systemPort,
            MultiValueMap<String, HttpCookie> cookies, Mono<List<Property>> authTokenMono, Set<String> disallowedHosts) {
        this.visitorId = visitorId;
        this.applicationOrgId = applicationOrgId;
        this.systemPort = systemPort;
        this.cookies = cookies;
        this.authTokenMono = authTokenMono;
        this.disallowedHosts = disallowedHosts;
    }
}
