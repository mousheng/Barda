package com.barda.sdk.util;

import static com.barda.sdk.util.IDUtils.generate;
import static com.barda.sdk.util.UriUtils.getRefererURI;
import static java.util.Optional.ofNullable;

import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseCookie.ResponseCookieBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.CommonConfig.Cookie;

import lombok.extern.slf4j.Slf4j;

/**
 * Cookie 帮助类。
 * 该类提供了一组静态方法来操作 Cookie，并与 ServerWebExchange 进行交互。
 */
@Component
@Slf4j
public class CookieHelper {

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 保存 Cookie。
     *
     * @param token  要保存的 Cookie 值。
     * @param exchange  ServerWebExchange 对象。
     */
    public void saveCookie(String token, ServerWebExchange exchange) {
        boolean isUsingHttps = Optional.ofNullable(getRefererURI(exchange.getRequest()))
                .map(a -> "https".equalsIgnoreCase(a.getScheme()))
                .orElse(false);
        ResponseCookieBuilder builder = ResponseCookie.from(getCookieName(), token)
                .path(exchange.getRequest().getPath().contextPath().value() + "/")
                .httpOnly(true)
                .secure(isUsingHttps)
                .sameSite(isUsingHttps ? "None" : "Lax");
        // 设置 Cookie 的 max-age
        Cookie cookie = commonConfig.getCookie();
        if (cookie.getMaxAgeInSeconds() >= 0) {
            builder.maxAge(cookie.getMaxAgeInSeconds());
        }

        if (commonConfig.isCloud()) {
            String topPrivateDomain = UriUtils.getTopPrivateDomain(exchange);
            builder.domain(topPrivateDomain);
        }
        exchange.getResponse().addCookie(builder.build());
    }

    /**
     * 获取 Cookie 中的 JWT 值。
     *
     * @param exchange  ServerWebExchange 对象。
     * @return JWT 值。
     */
    public String getCookieToken(ServerWebExchange exchange) {
        return getCookieValue(exchange, getCookieName(), "");
    }

    /**
     * 获取 Cookie 中的 JWT。
     *
     * @param exchange  ServerWebExchange 对象。
     * @return JWT。
     */
    @Nullable
    public String getJWT(ServerWebExchange exchange) {
        return getCookieValue(exchange, "JWT", null);
    }

    /**
     * 获取 Cookie 中的值。
     *
     * @param exchange  ServerWebExchange 对象。
     * @param cookieName  Cookie 名称。
     * @param defaultValue  默认值。
     * @return Cookie 值。
     */
    public String getCookieValue(ServerWebExchange exchange, String cookieName, String defaultValue) {
        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        return ofNullable(cookies.getFirst(cookieName))
                .map(HttpCookie::getValue)
                .orElse(defaultValue);
    }

    /**
     * 生成一个新的 Cookie 值。
     *
     * @return 新的 Cookie 值。
     */
    public static String generateCookieToken() {
        return generate();
    }

    /**
     * 获取 Cookie 名称。
     *
     * @return Cookie 名称。
     */
    public String getCookieName() {
        return commonConfig.getCookieName();
    }
}
