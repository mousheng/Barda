package com.barda.plugin.restapi.helpers;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

import java.text.ParseException;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.google.common.collect.Iterables;
import com.barda.sdk.plugin.restapi.auth.BasicAuthConfig;

import me.vzhilin.auth.DigestAuthenticator;
import me.vzhilin.auth.parser.ChallengeResponse;

/**
 * 一个提供身份验证帮助功能的实用类。
 * 它包含一些静态方法来帮助生成身份验证头部并检查响应是否需要摘要身份验证。
 */
public final class AuthHelper {

    /**
     * 私有构造函数，防止实例化。
     */
    private AuthHelper() {
    }

    /**
     * 生成一个消费者，将基本身份验证头部添加到 HTTP 头部中。
     *
     * @param basicAuthConfig 包含用户名和密码的基本身份验证配置
     * @return 一个消费者，将基本身份验证头部添加到 HTTP 头部中
     */
    public static Consumer<HttpHeaders> basicAuth(BasicAuthConfig basicAuthConfig) {
        return httpHeaders -> httpHeaders.setBasicAuth(basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
    }

    /**
     * 检查响应是否需要摘要身份验证。
     *
     * @param response 客户端响应
     * @return 如果响应需要摘要身份验证，则返回 true；否则返回 false
     */
    public static boolean shouldDigestAuth(ClientResponse response) {
        return response.statusCode() == HttpStatus.UNAUTHORIZED
                && Optional.ofNullable(Iterables.getFirst(response.headers().header(WWW_AUTHENTICATE), null))
                .map(header -> header.trim().toLowerCase())
                .map(header -> header.startsWith("digest"))
                .orElse(false);
    }

    /**
     * 生成一个消费者，将摘要身份验证头部添加到 HTTP 头部中。
     *
     * @param basicAuthConfig 包含用户名和密码的基本身份验证配置
     * @param response 客户端响应
     * @param httpMethod HTTP 方法
     * @param requestPath 请求路径
     * @return 一个消费者，将摘要身份验证头部添加到 HTTP 头部中
     * @throws ParseException 如果解析摘要质询头部时发生异常
     */
    public static Consumer<HttpHeaders> digestAuth(BasicAuthConfig basicAuthConfig, ClientResponse response, HttpMethod httpMethod,
            String requestPath) throws ParseException {
        DigestAuthenticator authenticator = new DigestAuthenticator(basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
        String receivedAuthenticateHeader = response.headers().header(WWW_AUTHENTICATE).get(0);
        authenticator.onResponseReceived(ChallengeResponse.of(receivedAuthenticateHeader), response.statusCode().value());
        String authorizationHeader = authenticator.authorizationHeader(httpMethod.name(), requestPath);
        return httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
    }
}
