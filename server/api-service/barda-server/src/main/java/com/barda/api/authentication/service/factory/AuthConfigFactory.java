package com.barda.api.authentication.service.factory;

import java.util.Set;

import com.barda.api.authentication.dto.AuthConfigRequest;
import com.barda.sdk.auth.AbstractAuthConfig;

/**
 * 身份验证配置工厂接口，用于创建和管理身份验证配置。
 */
public interface AuthConfigFactory {

    /**
     * 创建一个身份验证配置。
     *
     * @param authConfigRequest 身份验证配置请求。
     * @param enable            是否启用该身份验证配置。
     * @return 创建的身份验证配置。
     */
    AbstractAuthConfig build(AuthConfigRequest authConfigRequest, boolean enable);

    /**
     * 获取支持的身份验证类型。
     *
     * @return 支持的身份验证类型集合。
     */
    Set<String> supportAuthTypes();
}
