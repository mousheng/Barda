package com.barda.api.authentication.service;

import static com.barda.sdk.exception.BizError.AUTH_ERROR;
import static com.barda.sdk.exception.BizError.DISABLE_AUTH_CONFIG_FORBIDDEN;
import static com.barda.sdk.exception.BizError.USER_NOT_EXIST;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.barda.api.authentication.dto.AuthConfigRequest;
import com.barda.api.authentication.request.AuthRequestFactory;
import com.barda.api.authentication.request.oauth2.OAuth2RequestContext;
import com.barda.api.authentication.service.factory.AuthConfigFactory;
import com.barda.api.authentication.util.AuthenticationUtils;
import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.InvitationApiService;
import com.barda.api.usermanagement.OrgApiService;
import com.barda.api.usermanagement.UserApiService;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.authentication.AuthenticationService;
import com.barda.domain.authentication.FindAuthConfig;
import com.barda.domain.authentication.context.AuthRequestContext;
import com.barda.domain.authentication.context.FormAuthRequestContext;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.model.OrganizationDomain;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.organization.service.OrganizationService;
import com.barda.domain.user.model.AuthUser;
import com.barda.domain.user.model.Connection;
import com.barda.domain.user.model.ConnectionAuthToken;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.util.CookieHelper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 身份验证API的实现类。
 * 该类提供基于表单和OAuth2的身份验证功能，并处理用户的注册和登录。
 *
 */
@Service
@Slf4j
public class AuthenticationApiServiceImpl implements AuthenticationApiService {

    /**
     * 组织API服务。
     */
    @Autowired
    private OrgApiService orgApiService;

    /**
     * 组织服务。
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * 身份验证请求工厂。
     */
    @Autowired
    private AuthRequestFactory<AuthRequestContext> authRequestFactory;

    /**
     * 身份验证服务。
     */
    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 邀请API服务。
     */
    @Autowired
    private InvitationApiService invitationApiService;

    /**
     * 业务事件发布器。
     */
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 会话用户服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * Cookie帮助器。
     */
    @Autowired
    private CookieHelper cookieHelper;

    /**
     * 身份验证配置工厂。
     */
    @Autowired
    private AuthConfigFactory authConfigFactory;

    /**
     * 用户API服务。
     */
    @Autowired
    private UserApiService userApiService;

    /**
     * 组织成员服务。
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 基于表单的身份验证。
     *
     * @param loginId  登录ID
     * @param password 密码
     * @param source   身份验证来源
     * @param register 是否注册新用户
     * @param authId   身份验证ID
     * @return 已授权的用户
     */
    @Override
    public Mono<AuthUser> authenticateByForm(String loginId, String password, String source, boolean register, String authId) {
        return authenticate(authId, source, new FormAuthRequestContext(loginId, password, register));
    }

    /**
     * 基于OAuth2的身份验证。
     *
     * @param authId     身份验证ID
     * @param source     身份验证来源
     * @param code       OAuth2授权码
     * @param redirectUrl 重定向URL
     * @return 已授权的用户
     */
    @Override
    public Mono<AuthUser> authenticateByOauth2(String authId, String source, String code, String redirectUrl) {
        return authenticate(authId, source, new OAuth2RequestContext(code, redirectUrl));
    }

    /**
     * 身份验证。
     *
     * @param authId  身份验证ID
     * @param source  身份验证来源
     * @param context 身份验证上下文
     * @return 已授权的用户
     */
    protected Mono<AuthUser> authenticate(String authId, @Deprecated String source, AuthRequestContext context) {
        return Mono.defer(() -> {
                    if (StringUtils.isNotBlank(authId)) {
                        return authenticationService.findAuthConfigByAuthId(authId);
                    }
                    log.warn("source is deprecated and will be removed in the future, please use authId instead. {}", source);
                    return authenticationService.findAuthConfigBySource(source);
                })
                .doOnNext(findAuthConfig -> {
                    context.setAuthConfig(findAuthConfig.authConfig());
                    context.setOrgId(Optional.ofNullable(findAuthConfig.organization()).map(Organization::getId).orElse(null));
                })
                .then(authRequestFactory.build(context))
                .flatMap(authRequest -> authRequest.auth(context))
                .doOnNext(authorizedUser -> {
                    authorizedUser.setOrgId(context.getOrgId());
                    authorizedUser.setAuthContext(context);
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof BizException) {
                        return Mono.error(throwable);
                    }
                    log.error("user auth error.", throwable);
                    return ofError(AUTH_ERROR, "AUTH_ERROR");
                });
    }

    /**
     * 登录或注册。
     *
     * @param authUser     已授权的用户
     * @param exchange     WebExchange
     * @param invitationId 邀请ID
     * @return 空Mono
     */
    @Override
    public Mono<Void> loginOrRegister(AuthUser authUser, ServerWebExchange exchange,
            String invitationId) {
        return updateOrCreateUser(authUser)
                .delayUntil(user -> ReactiveSecurityContextHolder.getContext()
                        .doOnNext(securityContext -> securityContext.setAuthentication(AuthenticationUtils.toAuthentication(user))))
                // 保存令牌并设置Cookie
                .delayUntil(user -> {
                    String token = CookieHelper.generateCookieToken();
                    return sessionUserService.saveUserSession(token, user, authUser.getSource())
                            .then(Mono.fromRunnable(() -> cookieHelper.saveCookie(token, exchange)));
                })
                // 注册后处理
                .delayUntil(user -> {
                    if (user.getIsNewUser()) {
                        return onUserRegister(user);
                    }
                    return Mono.empty();
                })
                // 登录后处理
                .delayUntil(user -> onUserLogin(authUser.getOrgId(), user, authUser.getSource()))
                // 处理邀请
                .delayUntil(__ -> {
                    if (StringUtils.isBlank(invitationId)) {
                        return Mono.empty();
                    }
                    return invitationApiService.inviteUser(invitationId);
                })
                // 发布事件
                .then(businessEventPublisher.publishUserLoginEvent(authUser.getSource(), exchange));
    }

    /**
     * 更新或创建用户。
     *
     * @param authUser 已授权的用户
     * @return 用户Mono
     */
    private Mono<User> updateOrCreateUser(AuthUser authUser) {
        return findByAuthUser(authUser)
                .flatMap(findByAuthUser -> {
                    if (findByAuthUser.userExist()) {
                        User user = findByAuthUser.user();
                        updateConnection(authUser, user);
                        return userService.update(user.getId(), user);
                    }

                    if (authUser.getAuthContext().getAuthConfig().isEnableRegister()) {
                        return userService.createNewUserByAuthUser(authUser);
                    }
                    return Mono.error(new BizException(USER_NOT_EXIST, "USER_NOT_EXIST"));
                });
    }

    /**
     * 根据已授权的用户查找用户。
     *
     * @param authUser 已授权的用户
     * @return FindByAuthUser
     */
    protected Mono<FindByAuthUser> findByAuthUser(AuthUser authUser) {
        return userService.findByAuthUser(authUser)
                .map(user -> new FindByAuthUser(true, user))
                .defaultIfEmpty(new FindByAuthUser(false, null));
    }

    /**
     * 更新连接后重新认证。
     *
     * @param authUser 已授权的用户
     * @param user     用户
     */
    private void updateConnection(AuthUser authUser, User user) {

        String orgId = authUser.getOrgId();
        Connection oldConnection = getAuthConnection(authUser, user);
        if (StringUtils.isNotBlank(orgId) && !oldConnection.containOrg(orgId)) {  // already exist in user auth connection
            oldConnection.addOrg(orgId);
        }
        // 清理旧数据
        oldConnection.setAuthId(authUser.getAuthContext().getAuthConfig().getId());

        // 保存授权令牌，该令牌可能在将来的数据源或查询中使用。
        oldConnection.setAuthConnectionAuthToken(
                Optional.ofNullable(authUser.getAuthToken()).map(ConnectionAuthToken::of).orElse(null));
        oldConnection.setRawUserInfo(authUser.getRawUserInfo());
    }

    /**
     * 获取身份验证连接。
     *
     * @param authUser 已授权的用户
     * @param user     用户
     * @return 连接
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected Connection getAuthConnection(AuthUser authUser, User user) {
        return user.getConnections()
                .stream()
                .filter(connection -> authUser.getSource().equals(connection.getSource())
                        && connection.getRawId().equals(authUser.getUid()))
                .findFirst()
                .get();
    }

    /**
     * 注册后处理。
     *
     * @param user 用户
     * @return 空Mono
     */
    protected Mono<Void> onUserRegister(User user) {
        return organizationService.createDefault(user).then();
    }

    /**
     * 登录后处理。
     *
     * @param orgId   组织ID
     * @param user    用户
     * @param source  身份验证来源
     * @return 空Mono
     */
    protected Mono<Void> onUserLogin(String orgId, User user, String source) {
        if (StringUtils.isEmpty(orgId)) {
            return Mono.empty();
        }
        return orgApiService.tryAddUserToOrgAndSwitchOrg(orgId, user.getId()).then();
    }

    /**
     * 启用身份验证配置。
     *
     * @param authConfigRequest 身份验证配置请求
     * @return 布尔Mono
     */
    @Override
    public Mono<Boolean> enableAuthConfig(AuthConfigRequest authConfigRequest) {
        return checkIfAdmin()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .flatMap(orgMember -> organizationService.getById(orgMember.getOrgId()))
                .doOnNext(organization -> addOrUpdateNewAuthConfig(organization, authConfigFactory.build(authConfigRequest, true)))
                .flatMap(organization -> organizationService.update(organization.getId(), organization));
    }

    /**
     * 禁用身份验证配置。
     *
     * @param authId 身份验证ID
     * @return 布尔Mono
     */
    @Override
    public Mono<Boolean> disableAuthConfig(String authId) {
        return checkIfAdmin()
                .then(checkIfOnlyEffectiveCurrentUserConnections(authId))
                .then(sessionUserService.getVisitorOrgMemberCache())
                .flatMap(orgMember -> organizationService.getById(orgMember.getOrgId()))
                .doOnNext(organization -> disableAuthConfig(organization, authId))
                .flatMap(organization -> organizationService.update(organization.getId(), organization))
                .delayUntil(result -> {
                    if (result) {
                        return removeTokensByAuthId(authId);
                    }
                    return Mono.empty();
                });
    }

    /**
     * 根据身份验证ID删除令牌。
     *
     * @param authId 身份验证ID
     * @return 空Mono
     */
    private Mono<Void> removeTokensByAuthId(String authId) {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMapMany(orgMember -> orgMemberService.getOrganizationMembers(orgMember.getOrgId()))
                .map(OrgMember::getUserId)
                .flatMap(userId -> userApiService.getTokensByAuthId(userId, authId))
                .delayUntil(token -> sessionUserService.removeUserSession(token))
                .then();
    }

    /**
     * 检查是否为管理员。
     *
     * @return 空Mono
     */
    private Mono<Void> checkIfAdmin() {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    if (orgMember.isAdmin()) {
                        return Mono.empty();
                    }
                    return deferredError(BizError.NOT_AUTHORIZED, "NOT_AUTHORIZED");
                });
    }

    /**
     * 检查是否只有当前用户的有效连接是由指定的身份验证ID标识的。
     *
     * @param authId 身份验证ID
     * @return 空Mono
     */
    private Mono<Void> checkIfOnlyEffectiveCurrentUserConnections(String authId) {
        Mono<List<String>> userConnectionAuthConfigIdListMono = sessionUserService.getVisitor()
                .flatMapIterable(User::getConnections)
                .filter(connection -> StringUtils.isNotBlank(connection.getAuthId()))
                .map(Connection::getAuthId)
                .collectList();
        Mono<List<String>> orgAuthIdListMono = authenticationService.findAllAuthConfigs(true)
                .map(FindAuthConfig::authConfig)
                .map(AbstractAuthConfig::getId)
                .collectList();
        return Mono.zip(userConnectionAuthConfigIdListMono, orgAuthIdListMono)
                .delayUntil(tuple -> {
                    List<String> userConnectionAuthConfigIds = tuple.getT1();
                    List<String> orgAuthConfigIds = tuple.getT2();
                    userConnectionAuthConfigIds.retainAll(orgAuthConfigIds);
                    userConnectionAuthConfigIds.remove(authId);
                    if (CollectionUtils.isEmpty(userConnectionAuthConfigIds)) {
                        return Mono.error(new BizException(DISABLE_AUTH_CONFIG_FORBIDDEN, "DISABLE_AUTH_CONFIG_FORBIDDEN"));
                    }
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 禁用身份验证配置。
     *
     * @param organization  组织
     * @param authId        身份验证ID
     */
    private void disableAuthConfig(Organization organization, String authId) {
        Optional.of(organization)
                .map(Organization::getAuthConfigs)
                .orElse(Collections.emptyList())
                .stream()
                .filter(abstractAuthConfig -> Objects.equals(abstractAuthConfig.getId(), authId))
                .forEach(abstractAuthConfig -> abstractAuthConfig.setEnable(false));
    }

    /**
     * 如果来自新身份验证配置的源在组织的身份验证配置中存在，请更新它。否则，请添加它。
     *
     * @param organization     组织
     * @param newAuthConfig   新身份验证配置
     */
    private void addOrUpdateNewAuthConfig(Organization organization, AbstractAuthConfig newAuthConfig) {
        OrganizationDomain organizationDomain = organization.getOrganizationDomain();
        if (organizationDomain == null) {
            organizationDomain = new OrganizationDomain();
            organization.setOrganizationDomain(organizationDomain);
        }

        Map<String, AbstractAuthConfig> authConfigMap = organizationDomain.getConfigs()
                .stream()
                .collect(Collectors.toMap(AbstractAuthConfig::getId, Function.identity()));
        // 在组织中，来源可以唯一标识整个身份验证配置。
        AbstractAuthConfig old = authConfigMap.get(newAuthConfig.getId());
        if (old != null) {
            newAuthConfig.merge(old);
        }
        authConfigMap.put(newAuthConfig.getId(), newAuthConfig);
        organizationDomain.setConfigs(new ArrayList<>(authConfigMap.values()));
    }

    // 静态内部类

    /**
     * 该类用于表示通过授权详细信息找到的用户的结果。
     */
    protected record FindByAuthUser(boolean userExist, User user) {
    }

    /**
     * 该类用于表示将授权详细信息绑定到访客的结果。
     */
    protected record VisitorBindAuthConnectionResult(@Nullable String orgId, String visitorId) {
    }
}
