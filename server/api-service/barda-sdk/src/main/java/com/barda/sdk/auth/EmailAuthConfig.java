package com.barda.sdk.auth;

import com.barda.sdk.config.SerializeConfig;
import com.barda.sdk.encryption.RSACryptoServiceImpl;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

import static com.barda.sdk.auth.constants.AuthTypeConstants.FORM;
import static com.barda.sdk.constants.AuthSourceConstants.EMAIL;

/**
 * 该类表示电子邮件身份验证配置。
 * <p>
 * 该类继承自 AbstractAuthConfig 类，并使用了 Lombok 库来实现 getter、setter 和构造函数。
 */
@Getter
@Setter
public class EmailAuthConfig extends AbstractAuthConfig {
    /**
     * 是否启用 RSA。
     */
    private boolean enableRSA;
    /**
     * RSA公钥。
     */
    @JsonView(SerializeConfig.JsonViews.Public.class)
    private String publicKey;

    /**
     * 构造函数,默认启用RSA加密。
     *
     * @param id             ID
     * @param enable         是否启用
     * @param enableRegister 是否启用注册
     */
    public EmailAuthConfig(@Nullable String id, boolean enable, boolean enableRegister) {
        super(id, EMAIL, EMAIL, enable, enableRegister, FORM);
        this.enableRSA = true;
        this.publicKey = new RSACryptoServiceImpl().getPUblicKeyString();
    }

    @JsonCreator
    public EmailAuthConfig(@Nullable String id, boolean enable, boolean enableRegister, boolean enableRSA) {
        super(id, EMAIL, EMAIL, enable, enableRegister, FORM);
        this.enableRSA = enableRSA;
        if (enableRSA) {
            this.publicKey = new RSACryptoServiceImpl().getPUblicKeyString();
        }
    }
}
