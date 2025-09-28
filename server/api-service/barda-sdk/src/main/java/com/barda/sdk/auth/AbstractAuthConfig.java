package com.barda.sdk.auth;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Getter;
import lombok.Setter;

/**
 * 该抽象类表示一个抽象的身份验证配置。
 *
 * 该类使用了 Lombok 库来实现 getter、setter 和构造函数。
 * 该类还使用了 Jackson 库来实现 JSON 类型信息。
 */
@Getter
@Setter
@JsonTypeInfo(use = Id.NAME, property = "authType", visible = true)
public abstract class AbstractAuthConfig {

    protected String id;
    /**
     * 这里的来源应该对每一个身份验证源都唯一。
     * 例如，Google OAuth2、GitHub OAuth2 或组织的 CAS-SSO。
     */
    protected String source;
    protected String sourceName;

    protected Boolean enable;
    protected Boolean enableRegister;

    protected String authType;

    /**
     * 构造函数。
     */
    protected AbstractAuthConfig(@Nullable String id, String source, String sourceName, Boolean enable, Boolean enableRegister, String authType) {
        this.id = id;
        this.source = source;
        this.sourceName = sourceName;
        this.enable = enable;
        this.enableRegister = enableRegister;
        this.authType = authType;
    }

    /**
     * 获取 ID。
     *
     * 对于存储在 MongoDB 中的身份验证配置，将生成并存储一个 UUID。
     * 对于存储在 YAML 中的身份验证配置，请使用来源。
     */
    public String getId() {
        return ObjectUtils.firstNonNull(id, source);
    }

    /**
     * 获取是否启用。
     */
    public final boolean isEnable() {
        return BooleanUtils.isTrue(enable);
    }

    /**
     * 获取是否启用注册。
     */
    public final boolean isEnableRegister() {
        return BooleanUtils.isTrue(enableRegister);
    }

    /**
     * 获取身份验证类型。
     */
    public final String getAuthType() {
        return this.authType;
    }

    /**
     * 加密操作。
     *
     * 子类可以重写此方法来实现自己的加密逻辑。
     */
    public void doEncrypt(Function<String, String> encryptFunc) {
    }

    /**
     * 解密操作。
     *
     * 子类可以重写此方法来实现自己的解密逻辑。
     */
    public void doDecrypt(Function<String, String> decryptFunc) {
    }

    /**
     * 合并操作。
     *
     * 子类可以重写此方法来实现自己的合并逻辑。
     */
    public void merge(AbstractAuthConfig oldConfig) {
    }
}
