package com.barda.domain.user.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.http.codec.multipart.Part;
import org.springframework.web.server.ServerWebExchange;

import com.barda.domain.user.model.AuthUser;
import com.barda.domain.user.model.Connection;
import com.barda.domain.user.model.User;
import com.barda.domain.user.model.UserDetail;
import com.barda.infra.annotation.NonEmptyMono;
import com.barda.infra.mongo.MongoUpsertHelper.PartialResourceWithId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户服务接口，定义了与用户相关的操作。
 */
public interface UserService {

    /**
     * 创建新用户。
     * @param user 用户信息。
     * @return Mono<User> 新用户。
     */
    Mono<User> create(User user);

    /**
     * 更新用户信息。
     * @param userId 用户ID。
     * @param user 用户信息。
     * @return Mono<User> 更新后的用户。
     */
    Mono<User> update(String userId, User user);

    /**
     * 根据ID查询用户。
     * @param id 用户ID。
     * @return Mono<User> 查询到的用户。
     */
    Mono<User> findById(String id);

    /**
     * 根据ID集合查询用户。
     * @param ids 用户ID集合。
     * @return Mono<Map<String, User>> 查询到的用户集合。
     */
    @NonEmptyMono
    Mono<Map<String, User>> getByIds(Collection<String> ids);

    /**
     * 根据连接的来源和原始ID查询用户。
     * @param connectionSource 连接的来源。
     * @param connectionSourceUuid 原始ID。
     * @return Mono<User> 查询到的用户。
     */
    Mono<User> findBySourceAndId(String connectionSource, String connectionSourceUuid);

    /**
     * 保存用户的头像。
     * @param filePart 头像文件。
     * @param t2 用户信息。
     * @return Mono<Boolean> 保存是否成功。
     */
    Mono<Boolean> saveProfilePhoto(Part filePart, User t2);

    /**
     * 绑定用户的邮箱。
     * @param user 用户信息。
     * @param email 邮箱。
     * @return Mono<Boolean> 绑定是否成功。
     */
    Mono<Boolean> bindEmail(User user, String email);

    /**
     * 根据AuthUser查询用户。
     * @param authUser AuthUser。
     * @return Mono<User> 查询到的用户。
     */
    Mono<User> findByAuthUser(AuthUser authUser);

    /**
     * 根据AuthUser创建新用户。
     * @param authUser AuthUser。
     * @return Mono<User> 新用户。
     */
    Mono<User> createNewUserByAuthUser(AuthUser authUser);

    /**
     * 获取用户的头像。
     * @param exchange ServerWebExchange。
     * @param userId 用户ID。
     * @return Mono<Void> 获取头像的操作。
     */
    Mono<Void> getUserAvatar(ServerWebExchange exchange, String userId);

    /**
     * 为用户添加新的连接。
     * @param userId 用户ID。
     * @param connection 新连接。
     * @return Mono<Boolean> 添加是否成功。
     */
    Mono<Boolean> addNewConnection(String userId, Connection connection);

    /**
     * 删除用户的头像。
     * @param visitor 用户信息。
     * @return Mono<Void> 删除头像的操作。
     */
    Mono<Void> deleteProfilePhoto(User visitor);

    /**
     * 更新用户的密码。
     * @param userId 用户ID。
     * @param oldPassword 旧密码。
     * @param newPassword 新密码。
     * @return Mono<Boolean> 更新是否成功。
     */
    Mono<Boolean> updatePassword(String userId, String oldPassword, String newPassword);

    /**
     * 重置用户的密码。
     * @param userId 用户ID。
     * @return Mono<String> 重置后的新密码。
     */
    Mono<String> resetPassword(String userId);

    /**
     * 为用户设置密码。
     * @param userId 用户ID。
     * @param password 新密码。
     * @return Mono<Boolean> 设置是否成功。
     */
    Mono<Boolean> setPassword(String userId, String password);

    /**
     * 构建用户的详细信息。
     * @param user 用户信息。
     * @param withoutDynamicGroups 是否不包含动态组。
     * @return Mono<UserDetail> 用户的详细信息。
     */
    Mono<UserDetail> buildUserDetail(User user, boolean withoutDynamicGroups);

    /**
     * 在企业模式下标记用户为已删除状态并无效化连接。
     * @param userId 用户ID。
     * @return Mono<Boolean> 操作是否成功。
     */
    Mono<Boolean> markUserDeletedAndInvalidConnectionsAtEnterpriseMode(String userId);

    /**
     * 批量创建新用户。
     * @param users 用户信息集合。
     * @return Flux<User> 新创建的用户集合。
     */
    Flux<User> bulkCreateUser(Collection<User> users);

    /**
     * 批量更新用户。
     * @param users 用户信息集合。
     * @return Mono<Void> 批量更新的操作。
     */
    Mono<Void> bulkUpdateUser(Collection<PartialResourceWithId<User>> users);

    /**
     * 根据连接的来源和原始ID集合查询用户。
     * @param connectionSource 连接的来源。
     * @param connectionSourceUuids 原始ID集合。
     * @return Flux<User> 查询到的用户集合。
     */
    Flux<User> findBySourceAndIds(String connectionSource, Collection<String> connectionSourceUuids);

}
