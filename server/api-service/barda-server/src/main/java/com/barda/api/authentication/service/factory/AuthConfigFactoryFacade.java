package com.barda.api.authentication.service.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.barda.api.authentication.dto.AuthConfigRequest;
import com.barda.sdk.auth.AbstractAuthConfig;

/**
 * 身份验证配置工厂门面类，用于将多个身份验证配置工厂聚合成一个门面，并提供统一的API。
 */
@Primary
@Component
public class AuthConfigFactoryFacade implements AuthConfigFactory {

    /**
     * 身份验证配置工厂列表。
     */
    @Autowired
    private List<AuthConfigFactory> factories;

    /**
     * 身份验证配置工厂映射，用于根据身份验证类型快速查找对应的工厂。
     */
    private final Map<String, AuthConfigFactory> factoryMap = new HashMap<>();

    /**
     * 初始化方法，在Bean初始化时执行。
     * 遍历所有的身份验证配置工厂，并将它们添加到工厂映射中。
     */
    @PostConstruct
    public void init() {
        for (AuthConfigFactory factory : factories) {
            if (factory instanceof AuthConfigFactoryFacade) {
                continue;
            }
            for (String authType : factory.supportAuthTypes()) {
                factoryMap.putIfAbsent(authType, factory);
            }
        }
    }

    /**
     * 根据认证配置请求构建认证配置对象。
     *
     * @param authConfigRequest 认证配置请求对象
     * @param enable            是否启用
     * @return 构建的认证配置对象
     * @throws UnsupportedOperationException 如果未知的认证类型
     */
    @Override
    public AbstractAuthConfig build(AuthConfigRequest authConfigRequest, boolean enable) {
        AuthConfigFactory factory = factoryMap.get(authConfigRequest.getAuthType());
        if (factory == null) {
            throw new UnsupportedOperationException(authConfigRequest.getAuthType());
        }
        return factory.build(authConfigRequest, enable);
    }

    /**
     * 获取支持的认证类型集合。
     *
     * @return 支持的认证类型集合
     */
    @Override
    public Set<String> supportAuthTypes() {
        return factoryMap.keySet();
    }
}
