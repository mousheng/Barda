package com.barda.api.authentication.dto;

import static com.barda.sdk.util.IDUtils.generate;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 身份验证配置请求类。
 * 该类继承自 {@link HashMap}，并添加了一些 getter 方法来获取身份验证配置的属性。
 */
public class AuthConfigRequest extends HashMap<String, Object> {

    /**
     * 获取身份验证配置的 ID。
     * 如果 ID 存在，则返回该 ID，否则生成一个新的 ID。
     *
     * @return 身份验证配置的 ID
     */
    public String getId() {
        return ObjectUtils.firstNonNull(getString("id"), generate());
    }

    /**
     * 获取身份验证类型。
     *
     * @return 身份验证类型
     */
    public String getAuthType() {
        return getString("authType");
    }

    /**
     * 获取是否启用注册功能。
     * 如果在请求中未指定 "enableRegister" 属性，则默认返回 true。
     *
     * @return 是否启用注册功能
     */
    public boolean isEnableRegister() {
        return MapUtils.getBoolean(this, "enableRegister", true);
    }

    /**
     * 获取客户端 ID。
     * 如果在请求中未指定 "clientId" 属性，则返回 null。
     *
     * @return 客户端 ID
     */
    @Nullable
    public String getClientId() {
        return getString("clientId");
    }

    /**
     * 获取客户端密钥。
     * 如果在请求中未指定 "clientSecret" 属性，则返回 null。
     *
     * @return 客户端密钥
     */
    @Nullable
    public String getClientSecret() {
        return getString("clientSecret");
    }

    /**
     * 获取来源。
     * 如果在请求中未指定 "source" 属性，则返回默认值。
     *
     * @param defaultValue 默认值
     * @return 来源
     */
    public String getSource(String defaultValue) {
        String source = getString("source");
        if (StringUtils.isNotBlank(source)) {
            return source;
        }
        return defaultValue;
    }

    /**
     * 获取来源名称。
     * 如果在请求中未指定 "sourceName" 属性，则返回默认值。
     *
     * @param defaultValue 默认值
     * @return 来源名称
     */
    public String getSourceName(String defaultValue) {
        String sourceName = getString("sourceName");
        if (StringUtils.isNotBlank(sourceName)) {
            return sourceName;
        }
        return defaultValue;
    }

    /**
     * 获取字符串属性的值。
     *
     * @param key 属性键
     * @return 属性值
     */
    public String getString(String key) {
        return MapUtils.getString(this, key);
    }
}
