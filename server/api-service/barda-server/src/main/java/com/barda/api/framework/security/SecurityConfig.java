package com.barda.api.framework.security;


import com.barda.api.framework.filter.UserSessionPersistenceFilter;
import com.barda.api.home.SessionUserService;
import com.barda.domain.user.model.User;
import com.barda.infra.constant.NewUrl;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.util.CookieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

import javax.annotation.Nonnull;
import java.util.List;

import static com.barda.infra.constant.NewUrl.GITHUB_STAR;
import static com.barda.infra.constant.Url.*;
import static com.barda.sdk.constants.Authentication.ANONYMOUS_USER;
import static com.barda.sdk.constants.Authentication.ANONYMOUS_USER_ID;

/**
 * 一个用于配置 Spring Security 和 Reactive Method Security 的类。
 * 该类包含了安全相关的配置，如 CORS、认证入口点、访问拒绝处理器等。
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    /**
     * 注入 CommonConfig 类，用于获取通用配置
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 注入 SessionUserService 类，用于处理会话用户
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 注入 AccessDeniedHandler 类，用于处理访问被拒绝的情况
     */
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    /**
     * 注入 ServerAuthenticationEntryPoint 类，用于处理认证入口点
     */
    @Autowired
    private ServerAuthenticationEntryPoint serverAuthenticationEntryPoint;

    /**
     * 注入 CookieHelper 类，用于处理 Cookie
     */
    @Autowired
    private CookieHelper cookieHelper;

    /**
     * 创建 SecurityWebFilterChain，用于配置安全相关的功能
     *
     * @param http ServerHttpSecurity，用于配置安全相关的功能
     * @return 一个 SecurityWebFilterChain，表示安全相关的功能
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // 启用 CORS
        http.cors(cors -> cors.configurationSource(buildCorsConfigurationSource()))
                // 禁用 CSRF
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .csrf(csrf -> csrf.disable())
                // 配置匿名用户
                .anonymous(anonymous -> anonymous.principal(createAnonymousUser()))
                // 配置 HTTP Basic 认证
                .httpBasic(Customizer.withDefaults())
                // 配置授权
                .authorizeExchange(exchanges -> exchanges
                        .matchers(
                                // 以下路径匹配的请求将被允许
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, CUSTOM_AUTH + "/otp/send"),// 短信验证
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, CUSTOM_AUTH + "/phone/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, CUSTOM_AUTH + "/ldap/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, CUSTOM_AUTH + "/tp/login/**"),// 第三方登录
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, CUSTOM_AUTH + "/callback/tp/login/**"),// 第三方登录回调
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, CUSTOM_AUTH + "/form/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, INVITATION_URL + "/**"),// 邀请
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, CUSTOM_AUTH + "/logout"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.HEAD, STATE_URL + "/healthCheck"),
                                // 以下路径匹配的请求将被允许，但需要进行认证
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, CONFIG_URL),// 系统设置
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, CONFIG_URL + "/deploymentId"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, APPLICATION_URL + "/*/view"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, USER_URL + "/me"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, USER_URL + "/currentUser"),

                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, GROUP_URL + "/list"), // application view
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, QUERY_URL + "/execute"), // application view
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, ORGANIZATION_URL + "/*/datasourceTypes"), // datasource types
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, DATASOURCE_URL + "/jsDatasourcePlugins"),

                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, GITHUB_STAR),

                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/otp/send"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/phone/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/ldap/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/tp/login/**"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/jwt/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/callback/tp/login/**"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/form/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/cas/login"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.INVITATION_URL + "/**"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.CUSTOM_AUTH + "/logout"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.CONFIG_URL),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.CONFIG_URL + "/deploymentId"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.HEAD, NewUrl.STATE_URL + "/healthCheck"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.APPLICATION_URL + "/*/view"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.USER_URL + "/me"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.USER_URL + "/currentUser"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.GROUP_URL + "/list"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, NewUrl.QUERY_URL + "/execute"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.MATERIAL_URL + "/**"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.LIBRARY_URL + "/shared/**"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.LIBRARY_URL + "/org/**"),
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.ORGANIZATION_URL + "/*/datasourceTypes"), // datasource types
                                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, NewUrl.DATASOURCE_URL + "/jsDatasourcePlugins")
                        )
                        .permitAll()
                        // 以下路径匹配的请求将需要进行认证
                        .pathMatchers("/api/**")
                        .authenticated()
                        .pathMatchers("/test/**")
                        .authenticated()
                        // 以下路径匹配的请求将被允许
                        .pathMatchers("/**")
                        .permitAll()
                        // 应用过滤器
                        .anyExchange().authenticated()
                );

        // 配置异常处理
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(serverAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        );

        // 添加一个过滤器，用于处理会话持久化
        http.addFilterBefore(new UserSessionPersistenceFilter(sessionUserService, cookieHelper), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    /**
     * 启用 CORS
     */
    private CorsConfigurationSource buildCorsConfigurationSource() {
        CorsConfiguration skipCheckCorsForAll = skipCheckCorsForAll();
        CorsConfiguration skipCheckCorsForAllowListDomains = skipCheckCorsForAllowListDomains();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(USER_URL + "/me", skipCheckCorsForAll);
        source.registerCorsConfiguration(CONFIG_URL, skipCheckCorsForAll);
        source.registerCorsConfiguration(GROUP_URL + "/list", skipCheckCorsForAll);
        source.registerCorsConfiguration(QUERY_URL + "/execute", skipCheckCorsForAll);
        source.registerCorsConfiguration(APPLICATION_URL + "/*/view", skipCheckCorsForAll);
        source.registerCorsConfiguration(GITHUB_STAR, skipCheckCorsForAll);
        source.registerCorsConfiguration(ORGANIZATION_URL + "/*/datasourceTypes", skipCheckCorsForAll);
        source.registerCorsConfiguration(DATASOURCE_URL + "/jsDatasourcePlugins", skipCheckCorsForAll);

        source.registerCorsConfiguration(NewUrl.USER_URL + "/me", skipCheckCorsForAll);
        source.registerCorsConfiguration(NewUrl.CONFIG_URL, skipCheckCorsForAll);
        source.registerCorsConfiguration(NewUrl.GROUP_URL + "/list", skipCheckCorsForAll);
        source.registerCorsConfiguration(NewUrl.QUERY_URL + "/execute", skipCheckCorsForAll);
        source.registerCorsConfiguration(NewUrl.APPLICATION_URL + "/*/view", skipCheckCorsForAll);
        source.registerCorsConfiguration(NewUrl.ORGANIZATION_URL + "/*/datasourceTypes", skipCheckCorsForAll);
        source.registerCorsConfiguration(NewUrl.DATASOURCE_URL + "/jsDatasourcePlugins", skipCheckCorsForAll);

        source.registerCorsConfiguration("/**", skipCheckCorsForAllowListDomains);
        return source;
    }

    /**
     * 跳过对所有请求的CORS检查并返回一个CORS配置。
     * 该配置允许来自任何域的请求，并允许所有HTTP方法和头部。
     * 允许凭据。
     *
     * @return 跳过CORS检查的CORS配置
     */
    @Nonnull
    private CorsConfiguration skipCheckCorsForAll() {
        // 创建一个新的CORS配置
        CorsConfiguration skipForAllowlistDomains = new CorsConfiguration();
        // 允许来自任何域的请求
        skipForAllowlistDomains.setAllowedOriginPatterns(List.of("*"));
        // 允许所有HTTP方法
        skipForAllowlistDomains.setAllowedMethods(List.of("*"));
        // 允许所有头部
        skipForAllowlistDomains.setAllowedHeaders(List.of("*"));
        // 允许凭据
        skipForAllowlistDomains.setAllowCredentials(true);
        // 返回配置
        return skipForAllowlistDomains;
    }

    /**
     * 跳过对允许列表中的域的CORS检查并返回一个CORS配置。
     * 该配置允许来自{@link CommonConfig#getSecurity().getAllCorsAllowedDomains()}的域的请求，
     * 并允许所有HTTP方法和头部。
     * 允许凭据。
     *
     * @return 跳过CORS检查的CORS配置
     */
    @Nonnull
    private CorsConfiguration skipCheckCorsForAllowListDomains() {
        // 创建一个新的CORS配置
        CorsConfiguration skipForAllowlistDomains = new CorsConfiguration();
        // 允许来自允许列表中的域的请求
        skipForAllowlistDomains.setAllowedOriginPatterns(commonConfig.getSecurity().getAllCorsAllowedDomains());
        // 允许所有HTTP方法
        skipForAllowlistDomains.setAllowedMethods(List.of("*"));
        // 允许所有头部
        skipForAllowlistDomains.setAllowedHeaders(List.of("*"));
        // 允许凭据
        skipForAllowlistDomains.setAllowCredentials(true);
        // 返回配置
        return skipForAllowlistDomains;
    }

    /**
     * 创建一个{@link ForwardedHeaderTransformer} bean，
     * 该bean用于处理HTTP请求中的X-Forwarded-*头部。
     *
     * @return 一个配置了X-Forwarded-*头部处理的bean
     */
    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        // 创建并返回一个ForwardedHeaderTransformer bean
        return new ForwardedHeaderTransformer();
    }

    /**
     * 创建一个默认的匿名用户
     */
    private User createAnonymousUser() {
        User user = new User();
        user.setId(ANONYMOUS_USER_ID);
        user.setName(ANONYMOUS_USER);
        user.setIsAnonymous(true);
        return user;
    }
}
