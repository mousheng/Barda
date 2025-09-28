package com.barda.domain.organization.model;

import java.util.List;

import com.barda.sdk.auth.AbstractAuthConfig;

/**
 * 企业连接配置接口。
 * 该接口定义了获取企业连接配置的功能。
 */
public interface EnterpriseConnectionConfig {

    /**
     * 获取企业连接的配置列表。
     *
     * @return 包含企业连接配置的列表
     */
    List<AbstractAuthConfig> getConfigs();
}
