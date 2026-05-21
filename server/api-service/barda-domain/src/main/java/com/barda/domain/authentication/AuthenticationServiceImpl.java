package com.barda.domain.authentication;

import static com.barda.sdk.exception.BizError.LOG_IN_SOURCE_NOT_SUPPORTED;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.barda.domain.organization.model.OrgMember;
import com.barda.sdk.encryption.RSACryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.organization.service.OrganizationService;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.config.AuthProperties;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.constants.AuthSourceConstants;
import com.barda.sdk.constants.GlobalContext;
import com.barda.sdk.constants.WorkspaceMode;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 认证服务的实现类。
 *
 * 该类实现了 AuthenticationService 接口，并提供与认证相关的功能。
 * 它使用 Spring 框架的依赖注入来获取所需的服务和配置。
 */
@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    /**
     * RSA 加密服务。
     */
    @Autowired
    private RSACryptoService rsaCryptoService;

    /**
     * 组织服务。
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 认证属性。
     */
    @Autowired
    private AuthProperties authProperties;

    /**
     * 根据authId查找认证配置。
     *
     * @param authId 认证ID。
     * @return 包含认证配置的Mono。
     */
    @Override
    public Mono<FindAuthConfig> findAuthConfigByAuthId(String authId) {
        return findAuthConfig(abstractAuthConfig -> Objects.equals(authId, abstractAuthConfig.getId()));
    }

    /**
     * 根据source查找认证配置。
     *
     * @param source 认证来源。
     * @return 包含认证配置的Mono。
     * @deprecated 使用findAuthConfigByAuthId替代。
     */
    @Override
    @Deprecated
    public Mono<FindAuthConfig> findAuthConfigBySource(String source) {
        return findAuthConfig(abstractAuthConfig -> Objects.equals(source, abstractAuthConfig.getSource()));
    }

    /**
     * 通用方法，用于根据条件查找 FindAuthConfig。
     *
     * @param condition 用于匹配 FindAuthConfig 的条件
     * @return 匹配的 FindAuthConfig Mono 对象
     */
    private Mono<FindAuthConfig> findAuthConfig(Function<AbstractAuthConfig, Boolean> condition) {
        return findAllAuthConfigs(true)
                .filter(findAuthConfig -> condition.apply(findAuthConfig.authConfig()))
                .next()
                .switchIfEmpty(ofError(LOG_IN_SOURCE_NOT_SUPPORTED, "LOG_IN_SOURCE_NOT_SUPPORTED"));
    }

    /**
     * 查找所有认证配置。
     *
     * @param enableOnly 是否只包含启用的配置。
     * @return 包含所有认证配置的Flux。
     */
    @Override
    public Flux<FindAuthConfig> findAllAuthConfigs(boolean enableOnly) {
        return getOrgIdFromContext()
                .flatMapMany(orgIdFromCtx -> findAllAuthConfigsByOrgId(orgIdFromCtx, enableOnly))
                .switchIfEmpty(findAllAuthConfigsFallback(enableOnly));
    }

    /**
     * 查找所有认证配置，支持指定组织ID。
     *
     * @param enableOnly 是否只包含启用的配置。
     * @param orgId 组织ID，为null时委托给 {@link #findAllAuthConfigs(boolean)}。
     * @return 包含所有认证配置的Flux。
     */
    @Override
    public Flux<FindAuthConfig> findAllAuthConfigs(boolean enableOnly, @Nullable String orgId) {
        if (orgId != null) {
            return findAllAuthConfigsByOrgId(orgId, enableOnly)
                    .switchIfEmpty(findAllAuthConfigs(enableOnly));
        }
        return findAllAuthConfigs(enableOnly);
    }

    /**
     * 兜底查找：域匹配 → SAAS主组织 → 企业组织 → 默认EMAIL。
     */
    private Flux<FindAuthConfig> findAllAuthConfigsFallback(boolean enableOnly) {
        return findAllAuthConfigsByDomain()
                .switchIfEmpty(findAllAuthConfigsForSaasMode())
                .switchIfEmpty(findAllAuthConfigsForEnterpriseMode())
                .filter(findAuthConfig -> {
                    if (enableOnly) {
                        return findAuthConfig.authConfig().isEnable();
                    }
                    return true;
                })
                .defaultIfEmpty(new FindAuthConfig(
                        authProperties.getEmail().getRSA() ?
                                DEFAULT_AUTH_CONFIG :
                                DEFAULT_AUTH_CONFIG_DISABLE_RSA, null));
    }

    /**
     * 从当前请求的session上下文中获取用户所属组织的orgId。
     * 未登录时返回Mono.empty()，降级到域名/SAAS/企业模式的查找逻辑。
     */
    private Mono<String> getOrgIdFromContext() {
        return Mono.deferContextual(contextView -> {
            if (!contextView.hasKey(GlobalContext.CURRENT_ORG_MEMBER)) {
                return Mono.<String>empty();
            }
            return contextView.<Mono<OrgMember>>get(GlobalContext.CURRENT_ORG_MEMBER)
                    .filter(orgMember -> !orgMember.isInvalid())
                    .map(OrgMember::getOrgId);
        });
    }

    /**
     * 根据组织ID直接查找认证配置。
     */
    private Flux<FindAuthConfig> findAllAuthConfigsByOrgId(String orgId, boolean enableOnly) {
        return organizationService.getById(orgId)
                .flatMapIterable(organization -> {
                    List<AbstractAuthConfig> allConfigs = organization.getAuthConfigs();
                    List<AbstractAuthConfig> configs = allConfigs
                            .stream()
                            .filter(abstractAuthConfig -> !enableOnly || abstractAuthConfig.isEnable())
                            .collect(Collectors.toList());
                    boolean hasEmail = allConfigs.stream()
                            .anyMatch(c -> AuthSourceConstants.EMAIL.equals(c.getSource()));
                    if (!hasEmail) {
                        configs.add(authProperties.getEmail().getRSA() ?
                                DEFAULT_AUTH_CONFIG : DEFAULT_AUTH_CONFIG_DISABLE_RSA);
                    }
                    return configs.stream()
                            .map(abstractAuthConfig -> new FindAuthConfig(abstractAuthConfig, organization))
                            .collect(Collectors.toList());
                });
    }

    /**
     * 获取按域划分的 FindAuthConfig。
     *
     * @return FindAuthConfig Flux 对象
     */
    private Flux<FindAuthConfig> findAllAuthConfigsByDomain() {
        return organizationService.getByDomain()
                .flatMapIterable(organization -> {
                    List<AbstractAuthConfig> configs = new ArrayList<>(
                            organization.getAuthConfigs());
                    boolean hasEmail = configs.stream()
                            .anyMatch(c -> AuthSourceConstants.EMAIL.equals(c.getSource()));
                    if (!hasEmail) {
                        configs.add(authProperties.getEmail().getRSA() ?
                                DEFAULT_AUTH_CONFIG : DEFAULT_AUTH_CONFIG_DISABLE_RSA);
                    }
                    return configs.stream()
                            .map(abstractAuthConfig -> new FindAuthConfig(abstractAuthConfig, organization))
                            .collect(Collectors.toList());
                });
    }

    /**
     * 获取企业模式下的 FindAuthConfig。
     *
     * @return FindAuthConfig Flux 对象
     */
    protected Flux<FindAuthConfig> findAllAuthConfigsForEnterpriseMode() {
        if (commonConfig.getWorkspace().getMode() == WorkspaceMode.SAAS) {
            return Flux.empty();
        }
        return organizationService.getOrganizationInEnterpriseMode()
                .flatMapIterable(organization -> {
                    List<AbstractAuthConfig> configs = new ArrayList<>(
                            organization.getAuthConfigs());
                    boolean hasEmail = configs.stream()
                            .anyMatch(c -> AuthSourceConstants.EMAIL.equals(c.getSource()));
                    if (!hasEmail) {
                        configs.add(authProperties.getEmail().getRSA() ?
                                DEFAULT_AUTH_CONFIG : DEFAULT_AUTH_CONFIG_DISABLE_RSA);
                    }
                    return configs.stream()
                            .map(abstractAuthConfig -> new FindAuthConfig(abstractAuthConfig, organization))
                            .collect(Collectors.toList());
                });
    }

    /**
     * 获取 SaaS 模式下的 FindAuthConfig。
     *
     * @return FindAuthConfig Flux 对象
     */
    private Flux<FindAuthConfig> findAllAuthConfigsForSaasMode() {
        if (commonConfig.getWorkspace().getMode() == WorkspaceMode.SAAS) {
            return organizationService.getPrimaryOrganization()
                    .flatMapIterable(organization -> {
                        List<AbstractAuthConfig> configs = new ArrayList<>(
                                organization.getAuthConfigs());
                        boolean hasEmail = configs.stream()
                                .anyMatch(c -> AuthSourceConstants.EMAIL.equals(c.getSource()));
                        if (!hasEmail) {
                            configs.add(authProperties.getEmail().getRSA() ?
                                    DEFAULT_AUTH_CONFIG : DEFAULT_AUTH_CONFIG_DISABLE_RSA);
                        }
                        // organization 传 null：SAAS 模式下从主组织读取登录方式仅作为默认配置来源，
                        // 不代表通过默认页面注册的用户要加入主组织（他们会创建自己的组织）
                        return configs.stream()
                                .map(abstractAuthConfig -> new FindAuthConfig(abstractAuthConfig, null))
                                .collect(Collectors.toList());
                    });
        }
        return Flux.empty();
    }
}
