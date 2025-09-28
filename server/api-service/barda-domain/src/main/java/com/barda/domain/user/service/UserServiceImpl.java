package com.barda.domain.user.service;


import static com.google.common.collect.Sets.newHashSet;
import static com.barda.domain.user.model.UserDetail.ANONYMOUS_CURRENT_USER;
import static com.barda.sdk.constants.GlobalContext.CLIENT_IP;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static com.barda.sdk.util.ExceptionUtils.ofException;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.barda.domain.asset.model.Asset;
import com.barda.domain.asset.service.AssetService;
import com.barda.domain.authentication.AuthenticationService;
import com.barda.domain.authentication.context.FormAuthRequestContext;
import com.barda.domain.encryption.EncryptionService;
import com.barda.domain.group.model.Group;
import com.barda.domain.group.service.GroupMemberService;
import com.barda.domain.group.service.GroupService;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.user.model.AuthUser;
import com.barda.domain.user.model.Connection;
import com.barda.domain.user.model.User;
import com.barda.domain.user.model.User.TransformedUserInfo;
import com.barda.domain.user.model.UserDetail;
import com.barda.domain.user.model.UserState;
import com.barda.domain.user.repository.UserRepository;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.infra.mongo.MongoUpsertHelper.PartialResourceWithId;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.dynamic.Conf;
import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.constants.AuthSourceConstants;
import com.barda.sdk.constants.FieldName;
import com.barda.sdk.constants.WorkspaceMode;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.util.LocaleUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户服务实现类。
 * 提供用户相关的业务逻辑操作。
 * 包括用户的创建、更新、删除、绑定电子邮件、更新密码等功能。
 * 使用了多种服务类如资产服务、配置中心、加密服务等来完成这些操作。
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AssetService assetService;
    @Autowired
    private ConfigCenter configCenter;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;
    @Autowired
    private UserRepository repository;
    @Autowired
    private GroupMemberService groupMemberService;
    @Autowired
    private OrgMemberService orgMemberService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private CommonConfig commonConfig;
    @Autowired
    private AuthenticationService authenticationService;

    private Conf<Integer> avatarMaxSizeInKb;

    /**
     * 初始化方法，设置头像的最大尺寸。
     */
    @PostConstruct
    public void init() {
        avatarMaxSizeInKb = configCenter.asset().ofInteger("avatarMaxSizeInKb", 300);
    }

    /**
     * 创建新用户。
     *
     * @param user 要创建的用户对象
     * @return 创建后的用户对象
     */
    @Override
    public Mono<User> create(User user) {
        return repository.save(user);
    }

    /**
     * 根据用户ID查找用户。
     *
     * @param id 用户ID
     * @return 找到的用户对象
     */
    @Override
    public Mono<User> findById(String id) {
        if (id == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }
        return repository.findById(id);
    }

    /**
     * 根据一组用户ID查找用户。
     *
     * @param ids 用户ID集合
     * @return 用户ID与用户对象的映射
     */
    @Override
    public Mono<Map<String, User>> getByIds(Collection<String> ids) {
        Set<String> idSet = newHashSet(ids);
        return repository.findByIdIn(idSet)
                .collectList()
                .map(it -> it.stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()))
                );
    }

    /**
     * 根据来源和来源ID查找用户。
     *
     * @param source 来源
     * @param sourceUuid 来源ID
     * @return 找到的用户对象
     */
    @Override
    public Mono<User> findBySourceAndId(String source, String sourceUuid) {
        return repository.findByConnections_SourceAndConnections_RawId(source, sourceUuid);
    }

    /**
     * 保存用户头像。
     *
     * @param filePart 头像文件
     * @param user 用户对象
     * @return 是否保存成功
     */
    @Override
    public Mono<Boolean> saveProfilePhoto(Part filePart, User user) {
        String prevAvatar = ObjectUtils.defaultIfNull(user.getAvatar(), "");
        Mono<Asset> newAvatarMono = assetService.upload(filePart, avatarMaxSizeInKb.get(), true);
        return newAvatarMono
                .flatMap(newAvatar -> {
                    Mono<Boolean> updateUserAvatarMono = updateUserAvatar(newAvatar, user.getId());
                    if (StringUtils.isEmpty(prevAvatar)) {
                        return updateUserAvatarMono;
                    }
                    return assetService.remove(prevAvatar).then(updateUserAvatarMono);
                });
    }

    /**
     * 更新用户头像。
     *
     * @param newAvatar 新的头像资产对象。
     * @param userId 用户的唯一标识符。
     * @return 表示更新操作结果的 {@code Mono<Boolean>}，如果更新成功返回 {@code true}，否则返回 {@code false}。
     */
    private Mono<Boolean> updateUserAvatar(Asset newAvatar, String userId) {
        User user = new User();
        user.setAvatar(newAvatar.getId());
        return mongoUpsertHelper.updateById(user, userId);
    }


    /**
     * 更新用户信息。
     *
     * @param id 用户ID
     * @param updatedUser 更新后的用户对象
     * @return 更新后的用户对象
     */
    public Mono<User> update(String id, User updatedUser) {
        return mongoUpsertHelper.updateById(updatedUser, id)
                .flatMap(updated -> {
                    if (!updated) {
                        return ofError(BizError.NO_RESOURCE_FOUND, "NO_USER_FOUND", id);
                    }
                    return findById(id);
                });
    }

    /**
     * 根据认证用户信息查找用户。
     *
     * @param authUser 认证用户信息
     * @return 找到的用户对象
     */
    @Override
    public Mono<User> findByAuthUser(AuthUser authUser) {
        return findBySourceAndId(authUser.getSource(), authUser.getUid());
    }

    /**
     * 创建新用户（通过认证用户信息）。
     *
     * @param authUser 认证用户信息
     * @return 创建后的用户对象
     */
    @Override
    public Mono<User> createNewUserByAuthUser(AuthUser authUser) {
        User newUser = new User();
        newUser.setName(authUser.getUsername());
        newUser.setState(UserState.ACTIVATED);
        newUser.setIsEnabled(true);
        newUser.setTpAvatarLink(authUser.getAvatar());
        if (AuthSourceConstants.EMAIL.equals(authUser.getSource())
                && authUser.getAuthContext() instanceof FormAuthRequestContext formAuthRequestContext) {
            newUser.setPassword(encryptionService.encryptPassword(formAuthRequestContext.getPassword()));
        }
        Set<Connection> connections = newHashSet();
        Connection connection = authUser.toAuthConnection();
        connections.add(connection);
        newUser.setConnections(connections);
        newUser.setIsNewUser(true);
        return create(newUser);
    }

    /**
     * 获取用户头像。
     *
     * @param exchange 服务器网络交换对象
     * @param userId 用户ID
     * @return 返回空
     */
    @Override
    public Mono<Void> getUserAvatar(ServerWebExchange exchange, String userId) {
        return findById(userId)
                .flatMap(user -> assetService.makeImageResponse(exchange, user.getAvatar()));
    }

    /**
     * 绑定电子邮件到用户。
     *
     * @param user 用户对象
     * @param email 电子邮件
     * @return 是否绑定成功
     */
    @Override
    public Mono<Boolean> bindEmail(User user, String email) {
        Connection connection = Connection.builder()
                .source(AuthSourceConstants.EMAIL)
                .name(email)
                .rawId(email)
                .build();
        user.getConnections().add(connection);
        return repository.save(user)
                .then(Mono.just(true))
                .onErrorResume(throwable -> {
                    if (throwable instanceof DuplicateKeyException) {
                        return Mono.error(new BizException(BizError.ALREADY_BIND, "ALREADY_BIND", email, ""));
                    }
                    return Mono.error(throwable);
                });
    }

    /**
     * 添加新连接到用户。
     *
     * @param userId 用户ID
     * @param connection 新连接
     * @return 是否添加成功
     */
    @Override
    public Mono<Boolean> addNewConnection(String userId, Connection connection) {
        return findById(userId)
                .doOnNext(user -> user.getConnections().add(connection))
                .flatMap(repository::save)
                .then(Mono.just(true));
    }

    /**
     * 删除用户头像。
     *
     * @param visitor 用户对象
     * @return 返回空
     */
    @Override
    public Mono<Void> deleteProfilePhoto(User visitor) {
        String userAvatar = visitor.getAvatar();
        visitor.setAvatar(null);
        return repository.save(visitor).thenReturn(userAvatar)
                .flatMap(assetService::remove);
    }

    /**
     * 更新用户密码。
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    @Override
    public Mono<Boolean> updatePassword(String userId, String oldPassword, String newPassword) {
        return findById(userId)
                .<User> handle((user, sink) -> {
                    String password = user.getPassword();
                    if (StringUtils.isBlank(password)) {
                        sink.error(ofException(BizError.INVALID_PASSWORD, "INVALID_PASSWORD"));
                        return;
                    }
                    String originalEncryptPassword = user.getPassword();
                    if (!encryptionService.matchPassword(oldPassword, originalEncryptPassword)) {
                        sink.error(ofException(BizError.INVALID_PASSWORD, "INVALID_PASSWORD"));
                        return;
                    }
                    user.setPassword(encryptionService.encryptPassword(newPassword));
                    sink.next(user);
                })
                .flatMap(repository::save)
                .thenReturn(true);
    }

    /**
     * 重置用户密码。
     *
     * @param userId 用户ID
     * @return 新密码
     */
    @Override
    public Mono<String> resetPassword(String userId) {
        return findById(userId)
                .flatMap(user -> {
                    String password = user.getPassword();
                    if (StringUtils.isBlank(password)) {
                        return ofError(BizError.INVALID_PASSWORD, "PASSWORD_NOT_SET_YET");
                    }

                    String randomStr = generateNewRandomPwd();
                    user.setPassword(encryptionService.encryptPassword(randomStr));
                    return repository.save(user)
                            .thenReturn(randomStr);
                });
    }

    /**
     * 生成新的随机密码。
     *
     * @return 随机密码字符串
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Nonnull
    private static String generateNewRandomPwd() {
        char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}<>?")
                .toCharArray();
        return RandomStringUtils.random(12, 0, possibleCharacters.length - 1,
                false, false, possibleCharacters, new SecureRandom());
    }

    /**
     * 设置用户密码。
     *
     * @param userId 用户ID
     * @param password 新密码
     * @return 是否设置成功
     */
    @Override
    public Mono<Boolean> setPassword(String userId, String password) {
        return findById(userId)
                .map(user -> {
                    user.setPassword(encryptionService.encryptPassword(password));
                    return user;
                })
                .flatMap(repository::save)
                .thenReturn(true);
    }

    /**
     * 构建用户详情。
     *
     * @param user 用户对象
     * @param withoutDynamicGroups 是否排除动态组
     * @return 用户详情对象
     */
    @Override
    public Mono<UserDetail> buildUserDetail(User user, boolean withoutDynamicGroups) {
        if (user.isAnonymous()) {
            return Mono.just(ANONYMOUS_CURRENT_USER);
        }
        return Mono.deferContextual(contextView -> {
            String ip = contextView.getOrDefault(CLIENT_IP, "");
            Locale locale = LocaleUtils.getLocale(contextView);
            return orgMemberService.getCurrentOrgMember(user.getId())
                    .zipWhen(orgMember -> buildUserDetailGroups(user.getId(), orgMember, withoutDynamicGroups, locale))
                    .map(tuple2 -> {
                        OrgMember orgMember = tuple2.getT1();
                        List<Map<String, String>> groups = tuple2.getT2();
                        return UserDetail.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .avatarUrl(user.getAvatarUrl())
                                .email(convertEmail(user.getConnections()))
                                .ip(ip)
                                .groups(groups)
                                .extra(getUserDetailExtra(user, orgMember.getOrgId()))
                                .build();
                    });
        });
    }

    /**
     * 在企业模式下，标记用户已删除并无效化相关连接。
     *
     * @param userId 用户ID
     * @return 是否成功标记
     */
    @Override
    public Mono<Boolean> markUserDeletedAndInvalidConnectionsAtEnterpriseMode(String userId) {
        if (commonConfig.getWorkspace().getMode() == WorkspaceMode.SAAS) {
            return Mono.just(false);
        }
        return repository.findById(userId)
                .flatMap(user -> {
                    user.markAsDeleted();
                    return mongoUpsertHelper.updateById(user, userId);
                });
    }

    /**
     * 获取用户详细信息的额外数据。
     *
     * @param user 用户对象
     * @param orgId 组织ID
     * @return 额外数据的Map
     */
    protected Map<String, Object> getUserDetailExtra(User user, String orgId) {
        return Optional.ofNullable(user.getOrgTransformedUserInfo())
                .map(orgTransformedUserInfo -> orgTransformedUserInfo.get(orgId))
                .map(TransformedUserInfo::extra)
                .orElse(convertConnections(user.getConnections()));
    }

    /**
     * 构建用户详细信息中的组信息。
     *
     * @param userId 用户ID
     * @param orgMember 组织成员对象
     * @param withoutDynamicGroups 是否排除动态组
     * @param locale 本地化信息
     * @return 组信息列表
     */
    protected Mono<List<Map<String, String>>> buildUserDetailGroups(String userId, OrgMember orgMember, boolean withoutDynamicGroups,
            Locale locale) {
        String orgId = orgMember.getOrgId();
        Flux<Group> groups;
        if (orgMember.isAdmin()) {
            groups = groupService.getByOrgId(orgId).sort();
        } else {
            if (withoutDynamicGroups) {
                groups = groupMemberService.getNonDynamicUserGroupIdsInOrg(orgId, userId).flatMapMany(l -> groupService.getByIds(l));
            } else {
                groups = groupMemberService.getUserGroupIdsInOrg(orgId, userId).flatMapMany(l -> groupService.getByIds(l));
            }
        }
        return groups.filter(group -> !group.isAllUsersGroup())
                .map(group -> Map.of("groupId", Objects.toString(group.getId(), ""), "groupName", group.getName(locale)))
                .collectList();
    }

    /**
     * 转换用户的连接信息。
     *
     * @param connections 用户的连接集合
     * @return 连接信息的Map
     */
    protected Map<String, Object> convertConnections(Set<Connection> connections) {
        return connections.stream()
                .filter(connection -> !AuthSourceConstants.EMAIL.equals(connection.getSource()) &&
                        !AuthSourceConstants.PHONE.equals(connection.getSource()))
                .collect(Collectors.toMap(Connection::getSource, Connection::getRawUserInfo));
    }

    /**
     * 转换用户的电子邮件信息。
     *
     * @param connections 用户的连接集合
     * @return 电子邮件字符串
     */
    protected String convertEmail(Set<Connection> connections) {
        return connections.stream().filter(connection -> AuthSourceConstants.EMAIL.equals(connection.getSource()))
                .findFirst()
                .map(Connection::getName)
                .orElse("");
    }

    /**
     * 批量创建用户。
     *
     * @param users 用户集合
     * @return 创建的用户对象流
     */
    @Override
    public Flux<User> bulkCreateUser(Collection<User> users) {
        return repository.saveAll(users);
    }

    /**
     * 批量更新用户。
     *
     * @param partialResourceWithIds 部分资源ID集合
     * @return 返回空
     */
    @Override
    public Mono<Void> bulkUpdateUser(Collection<PartialResourceWithId<User>> partialResourceWithIds) {
        return mongoUpsertHelper.bulkUpdate(partialResourceWithIds).then();
    }

    /**
     * 根据来源和来源ID集合查找用户。
     *
     * @param connectionSource 连接来源
     * @param connectionSourceUuids 来源ID集合
     * @return 找到的用户对象流
     */
    @Override
    public Flux<User> findBySourceAndIds(String connectionSource, Collection<String> connectionSourceUuids) {
        return repository.findByConnections_SourceAndConnections_RawIdIn(connectionSource, connectionSourceUuids);
    }

}
