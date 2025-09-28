package com.barda.domain.authentication.context;

import lombok.Getter;

/**
 * 基于表单的认证请求上下文类。
 *
 * 该类继承自 AuthRequestContext 并添加了特定于基于表单的认证的属性和方法。
 */
@Getter
public class FormAuthRequestContext extends AuthRequestContext {

    /**
     * 用于登录的 ID，可以是电话号码或电子邮件。
     */
    private final String loginId;

    /**
     * 密码。
     */
    private final String password;

    /**
     * 指示是否可以注册。
     */
    private final boolean register;

    /**
     * 构造函数。
     *
     * @param loginId 用于登录的 ID，可以是电话号码或电子邮件
     * @param password 密码
     * @param register 指示是否为注册操作
     */
    public FormAuthRequestContext(String loginId, String password, boolean register) {
        this.loginId = loginId;
        this.password = password;
        this.register = register;
    }
}
