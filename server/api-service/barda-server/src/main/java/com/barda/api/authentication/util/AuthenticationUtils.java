package com.barda.api.authentication.util;

import static java.util.Collections.emptyMap;
import static reactor.core.scheduler.Schedulers.newBoundedElastic;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.ImmutableSet;
import com.barda.domain.user.model.User;

import reactor.core.scheduler.Scheduler;

/**
 * 身份验证相关的实用工具类。
 * 该类提供一些静态方法来处理身份验证相关的操作，例如将 {@link User} 对象转换为 {@link Authentication} 对象。
 */
public final class AuthenticationUtils {

    /**
     * 仅用于身份验证的线程池的大小。
     * 该值可以根据需要进行调整。
     */
    public static final int JUST_AUTH_THREAD_POOL_SIZE = 50;

    /**
     * 用于执行身份验证请求的线程池。
     * 它使用有界弹性线程池，初始线程数为 {@link #JUST_AUTH_THREAD_POOL_SIZE}，最大线程数为 5000，线程名称前缀为 "auth-worker"。
     */
    public static final Scheduler AUTH_REQUEST_THREAD_POOL = newBoundedElastic(JUST_AUTH_THREAD_POOL_SIZE, 5000, "auth-worker");

    /**
     * 将 {@link User} 对象转换为 {@link Authentication} 对象。
     * 该方法返回一个匿名内部类实现的 {@link Authentication} 接口，
     * 并为其提供必要的属性和方法。
     *
     * @param user 要转换的 {@link User} 对象
     * @return 转换后的 {@link Authentication} 对象
     */
    public static Authentication toAuthentication(User user) {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                // 为用户授予 "ROLE_USER" 角色
                return ImmutableSet.of((GrantedAuthority) () -> "ROLE_USER");
            }

            @Override
            public Object getCredentials() {
                // 凭证为空字符串
                return "";
            }

            @Override
            public Object getDetails() {
                // 详细信息为空 Map
                return emptyMap();
            }

            @Override
            public Object getPrincipal() {
                // 主体为 User 对象
                return user;
            }

            @Override
            public boolean isAuthenticated() {
                // 假设用户已通过身份验证
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) {
                // 该方法不做任何操作
            }

            @Override
            public String getName() {
                // 获取 User 的名称
                return user.getName();
            }
        };
    }
}
