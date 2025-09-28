package com.barda.api.authentication.dto;

import static com.barda.sdk.util.ExceptionUtils.ofError;

import org.apache.commons.lang.StringUtils;

import com.barda.api.framework.view.ResponseView;
import com.barda.sdk.exception.BizError;

import reactor.core.publisher.Mono;

/**
 * 组织域检查结果记录类。
 * 该类使用 Java 14 的记录（Record）特性来定义数据类。
 */
public record OrganizationDomainCheckResult(String redirectDomain, boolean needBind) {

    /**
     * 创建一个表示操作成功的 {@link OrganizationDomainCheckResult} 实例。
     * 重定向域名为空字符串，不需要绑定。
     *
     * @return 成功的 {@link OrganizationDomainCheckResult} 实例
     */
    public static OrganizationDomainCheckResult success() {
        return new OrganizationDomainCheckResult("", false);
    }

    /**
     * 判断是否需要重定向。
     * 如果重定向域名不为空，则返回 true。
     *
     * @return 是否需要重定向
     */
    public boolean needRedirect() {
        return StringUtils.isNotBlank(redirectDomain);
    }

    /**
     * 获取是否需要绑定。
     * 直接返回 {@link #needBind} 的值。
     *
     * @return 是否需要绑定
     */
    public boolean needBind() {
        return needBind;
    }

    /**
     * 创建一个表示需要重定向的 {@link OrganizationDomainCheckResult} 实例。
     * 重定向域名为指定值，不需要绑定。
     *
     * @param redirectDomain 重定向域名
     * @return 需要重定向的 {@link OrganizationDomainCheckResult} 实例
     */
    public static OrganizationDomainCheckResult redirect(String redirectDomain) {
        return new OrganizationDomainCheckResult(redirectDomain, false);
    }

    /**
     * 创建一个表示需要绑定的 {@link OrganizationDomainCheckResult} 实例。
     * 重定向域名为空字符串，需要绑定。
     *
     * @return 需要绑定的 {@link OrganizationDomainCheckResult} 实例
     */
    public static OrganizationDomainCheckResult bind() {
        return new OrganizationDomainCheckResult("", true);
    }

    /**
     * 构建组织域检查结果的视图。
     * 如果需要重定向，返回一个包含重定向 URI 的成功视图。
     * 如果需要绑定，返回一个包含错误信息的错误视图。
     * 如果不需要重定向和绑定，返回一个空的 Mono。
     *
     * @return 组织域检查结果的视图
     */
    public Mono<ResponseView<?>> buildOrganizationDomainCheckView() {
        if (needRedirect()) {
            return Mono.just(ResponseView.success(BizError.REDIRECT.getBizErrorCode(),
                    RedirectView.builder()
                            .redirectUri("https://" + redirectDomain())
                            .build()));
        }
        if (needBind()) {
            return ofError(BizError.NEED_BIND_THIRD_PARTY_CONNECTION, "NEED_BIND_THIRD_PARTY_CONNECTION");
        }
        return Mono.empty();
    }
}
