package com.barda.api.authentication.request;

import static com.barda.sdk.exception.BizError.AUTH_ERROR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.barda.domain.authentication.context.AuthRequestContext;
import com.barda.sdk.exception.BizException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 身份验证请求工厂门面类，用于根据不同类型的身份验证配置创建对应的身份验证请求。
 * 该类实现了 {@link AuthRequestFactory} 接口，并使用 Spring 进行管理。
 * 它通过将所有实现了 {@link AuthRequestFactory} 接口的类注入到 {@link #authRequestFactories} 列表中，
 * 并在 {@link #init()} 方法中构建一个映射表 {@link #authRequestFactoryMap}，
 * 方便根据身份验证类型快速获取对应的身份验证请求工厂。
 *
 */
@Slf4j
@Primary
@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class AuthRequestFactoryFacade implements AuthRequestFactory<AuthRequestContext> {

    /**
     * 所有实现了 {@link AuthRequestFactory} 接口的类列表。
     */
    @Autowired
    private List<AuthRequestFactory> authRequestFactories;

    /**
     * 身份验证类型与其对应的身份验证请求工厂的映射表。
     */
    private final Map<String, AuthRequestFactory<AuthRequestContext>> authRequestFactoryMap = new HashMap<>();

    /**
     * 初始化方法，在 Spring 容器初始化时被调用。
     * 它将 {@link #authRequestFactories} 列表中的所有身份验证请求工厂构建成映射表。
     */
    @PostConstruct
    public void init() {
        for (AuthRequestFactory<AuthRequestContext> authRequestFactory : authRequestFactories) {
            if (authRequestFactory instanceof AuthRequestFactoryFacade) {
                continue;
            }
            for (String authType : authRequestFactory.supportedAuthTypes()) {
                if (authRequestFactoryMap.containsKey(authType)) {
                    throw new RuntimeException(String.format("duplicate authRequestFactory found for same authType: %s", authType));
                }
                authRequestFactoryMap.put(authType, authRequestFactory);
            }
        }
        log.info("find auth types:{}", authRequestFactoryMap.keySet());
    }

    /**
     * 构建身份验证请求。
     * 它根据 {@link AuthRequestContext} 中的身份验证类型从 {@link #authRequestFactoryMap} 获取对应的身份验证请求工厂，
     * 并使用该工厂构建身份验证请求。
     *
     * @param context 身份验证请求上下文
     * @return 身份验证请求的 Mono 对象
     */
    @Override
    public Mono<AuthRequest> build(AuthRequestContext context) {
        return Mono.defer(() -> {
            AuthRequestFactory<AuthRequestContext> authRequestFactory = authRequestFactoryMap.get(context.getAuthConfig().getAuthType());
            if (authRequestFactory == null) {
                return Mono.error(new BizException(AUTH_ERROR, "AUTH_ERROR"));
            }
            return authRequestFactory.build(context);
        });
    }

    /**
     * 获取所有支持的身份验证类型。
     * 由于 {@link AuthRequestFactoryFacade} 并不直接实现此方法，因此返回一个空集合。
     *
     * @return 空集合
     */
    @Override
    public Set<String> supportedAuthTypes() {
        return new HashSet<>(0);
    }
}
