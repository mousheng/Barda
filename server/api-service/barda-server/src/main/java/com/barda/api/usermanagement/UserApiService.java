package com.barda.api.usermanagement;

import static com.barda.sdk.exception.BizError.UNSUPPORTED_OPERATION;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserService;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.user.model.Connection;
import com.barda.domain.user.model.User;
import com.barda.domain.user.model.UserDetail;
import com.barda.domain.user.repository.UserRepository;
import com.barda.domain.user.service.UserService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户API的服务类。
 *
 */
@Service
@Slf4j
public class UserApiService {

    /**
     * 用于获取当前会话用户的服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 组织成员相关的服务。
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 用户相关的服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 用户仓库。
     */
    @Autowired
    private UserRepository repository;

    /**
     * 获取用户的详细信息。
     *
     * @param userId 用户ID
     * @return 用户的详细信息
     */
    public Mono<UserDetail> getUserDetailById(String userId) {
        return checkAdminPermissionAndUserBelongsToCurrentOrg(userId)
                .then(userService.findById(userId)
                        .flatMap(user -> userService.buildUserDetail(user, false)));
    }

    /**
     * 检查管理员权限并检查用户是否属于当前组织。
     *
     * @param userId 用户ID
     * @return 如果检查通过，返回空的Mono，否则返回包含{@link BizError#UNSUPPORTED_OPERATION}的Mono
     */
    private Mono<Void> checkAdminPermissionAndUserBelongsToCurrentOrg(String userId) {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    if (!orgMember.isAdmin()) {
                        return ofError(UNSUPPORTED_OPERATION, "BAD_REQUEST");
                    }
                    return orgMemberService.getOrgMember(orgMember.getOrgId(), userId)
                            .hasElement()
                            .flatMap(hasElement -> {
                                if (hasElement) {
                                    return Mono.empty();
                                }
                                return ofError(UNSUPPORTED_OPERATION, "BAD_REQUEST");
                            });
                });
    }

    /**
     * 重置用户的密码。
     *
     * @param userId 用户ID
     * @return 新生成的密码
     */
    public Mono<String> resetPassword(String userId) {
        return checkAdminPermissionAndUserBelongsToCurrentOrg(userId)
                .then(userService.resetPassword(userId));
    }

    // ========================== TOKEN OPERATIONS START ==========================

    /**
     * 保存令牌。
     *
     * @param userId 用户ID
     * @param source 来源
     * @param token  令牌
     * @return 空的Mono
     */
    public Mono<Void> saveToken(String userId, String source, String token) {
        return repository.findById(userId)
                .doOnNext(user -> user.getConnections().stream()
                        .filter(connection -> connection.getSource().equals(source))
                        .forEach(connection -> connection.addToken(token)))
                .flatMap(repository::save)
                .then();
    }

    /**
     * 移除令牌。
     *
     * @param userId 用户ID
     * @param token  令牌
     * @return 空的Mono
     */
    public Mono<Void> removeToken(String userId, String token) {
        return repository.findById(userId)
                .doOnNext(user -> removeToken(user, token))
                .flatMap(repository::save)
                .then();
    }

    /**
     * 移除令牌。
     *
     * @param user  用户对象。
     * @param token 要移除的令牌。
     */
    private void removeToken(User user, String token) {
        user.getConnections().forEach(connection -> connection.removeToken(token));
    }


    /**
     * 获取用户的令牌。
     *
     * @param userId 用户ID
     * @param authId 认证ID
     * @return 用户的令牌
     */
    public Flux<String> getTokensByAuthId(String userId, String authId) {
        return repository.findById(userId)
                .flatMapIterable(User::getConnections)
                .filter(connection -> Objects.equals(connection.getAuthId(), authId))
                .flatMapIterable(Connection::getTokens);
    }

    /**
     * 移除无效的令牌。
     *
     * 注意：Redis中的令牌在没有访问时会过期，而MongoDB中的令牌不会过期。
     * 因此，我们需要清理无效的令牌。
     *
     * @param userId 用户ID
     * @return 空的Mono
     */
    public Mono<Void> removeInvalidTokens(String userId) {
        return repository.findById(userId)
                .delayUntil(user -> getInvalidTokens(user).doOnNext(invalidToken -> removeToken(user, invalidToken)))
                .flatMap(repository::save)
                .then();
    }

    /**
     * 获取无效的令牌。
     *
     * @param user 用户
     * @return 无效的令牌
     */
    private Flux<String> getInvalidTokens(User user) {
        Set<String> allTokens = user.getConnections().stream()
                .map(Connection::getTokens)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        return Flux.fromIterable(allTokens)
                // 令牌在Redis中不存在
                .filterWhen(token -> sessionUserService.tokenExist(token).map(aBoolean -> !aBoolean));
    }

    // ========================== TOKEN OPERATIONS END ==========================
}
