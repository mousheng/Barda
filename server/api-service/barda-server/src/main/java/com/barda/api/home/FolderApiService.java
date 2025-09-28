package com.barda.api.home;

import static com.barda.infra.util.MonoUtils.emptyIfNull;
import static com.barda.sdk.exception.BizError.FOLDER_OPERATE_NO_PERMISSION;
import static com.barda.sdk.exception.BizError.FOLDER_NOT_EXIST;
import static com.barda.sdk.exception.BizError.ILLEGAL_FOLDER_PERMISSION_ID;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.api.application.view.ApplicationInfoView;
import com.barda.api.application.view.ApplicationPermissionView;
import com.barda.api.permission.PermissionHelper;
import com.barda.api.permission.view.PermissionItemView;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.model.ApplicationType;
import com.barda.domain.folder.model.Folder;
import com.barda.domain.folder.model.FolderElement;
import com.barda.domain.folder.service.ElementNode;
import com.barda.domain.folder.service.FolderElementRelationService;
import com.barda.domain.folder.service.FolderNode;
import com.barda.domain.folder.service.FolderService;
import com.barda.domain.folder.service.Node;
import com.barda.domain.folder.service.Tree;
import com.barda.domain.group.service.GroupService;
import com.barda.domain.interaction.UserFolderInteraction;
import com.barda.domain.interaction.UserFolderInteractionService;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.service.OrganizationService;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用于管理文件夹的API服务。
 */
@Service
public class FolderApiService {

    /**
     * 默认文件夹排序规则：按最后查看时间降序，然后按名称升序。
     */
    private static final Comparator<Node<ApplicationInfoView, FolderInfoView>> DEFAULT_COMPARATOR =
            // 按上次查看时间反转进行比较。
            Comparator.comparingLong((ToLongFunction<Node<ApplicationInfoView, FolderInfoView>>) node -> {
                        if (node instanceof ElementNode<ApplicationInfoView, FolderInfoView> elementNode) {
                            return elementNode.getSelf().getLastViewTime();
                        }
                        return ((FolderNode<ApplicationInfoView, FolderInfoView>) node).getSelf().getLastViewTime();
                    })
                    .reversed()
                    // 按名称进行比较。
                    .thenComparing(node -> {
                        if (node instanceof ElementNode<ApplicationInfoView, FolderInfoView> elementNode) {
                            return elementNode.getSelf().getName();
                        }
                        return ((FolderNode<ApplicationInfoView, FolderInfoView>) node).getSelf().getName();
                    });

    /**
     * 用于管理文件夹的服务。
     */
    @Autowired
    private FolderService folderService;

    /**
     * 用于获取当前会话用户的服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 用于检查当前组织是否为开发者的服务。
     */
    @Autowired
    private OrgDevChecker orgDevChecker;

    /**
     * 用于管理用户主页的API服务。
     */
    @Autowired
    private UserHomeApiService userHomeApiService;

    /**
     * 用于管理文件夹元素关系的服务。
     */
    @Autowired
    private FolderElementRelationService folderElementRelationService;

    /**
     * 用于管理资源权限的服务。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 用于辅助检查资源权限的帮助类。
     */
    @Autowired
    private PermissionHelper permissionHelper;

    /**
     * 用于管理分组的服务。
     */
    @Autowired
    private GroupService groupService;

    /**
     * 用于管理用户的服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 用于管理组织的服务。
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * 用于管理用户文件夹交互的服务。
     */
    @Autowired
    private UserFolderInteractionService userFolderInteractionService;

    /**
     * 创建一个新的文件夹。
     *
     * @param folder 要创建的新文件夹。
     * @return 新创建的文件夹的视图。
     */
    public Mono<FolderInfoView> create(Folder folder) {
        if (StringUtils.isBlank(folder.getName())) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "FOLDER_NAME_EMPTY"));
        }
        return orgDevChecker.checkCurrentOrgDev()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .delayUntil(orgMember -> {
                    if (StringUtils.isBlank(folder.getParentFolderId())) {
                        return Mono.empty();
                    }
                    return checkFolderExist(folder.getParentFolderId())
                            .flatMap(parent -> checkFolderCurrentOrg(parent, orgMember.getOrgId()));
                })
                .delayUntil(orgMember -> checkFolderNameUnique(folder.getParentFolderId(), folder.getName(), orgMember.getOrgId()))
                .flatMap(orgMember -> {
                    folder.setOrganizationId(orgMember.getOrgId());
                    folder.setCreatedBy(orgMember.getUserId());
                    return folderService.create(folder);
                })
                .flatMap(f -> buildFolderInfoView(f, true, true));
    }

    /**
     * 检查文件夹是否存在。
     *
     * @param folderId 要检查的文件夹ID。
     * @return 若文件夹存在，则返回该文件夹的Mono；否则返回错误的Mono。
     */
    public Mono<Folder> checkFolderExist(String folderId) {
        return folderService.findById(folderId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BizException(FOLDER_NOT_EXIST, "FOLDER_NOT_EXIST", folderId))));
    }

    /**
     * 用于检查指定文件夹是否属于当前组织。
     *
     * @param folder 要检查的目标文件夹。
     * @param currentOrgId 当前组织的ID。
     * @return 如果文件夹属于当前组织，则返回空的Mono；
     *         否则，返回包含BizException的Mono，其中包含文件夹不存在的错误信息。
     */
    public Mono<Void> checkFolderCurrentOrg(Folder folder, String currentOrgId) {
        if (currentOrgId.equals(folder.getOrganizationId())) {
            return Mono.empty();
        }
        return Mono.error(new BizException(FOLDER_NOT_EXIST, "FOLDER_NOT_EXIST", folder.getId()));
    }

    /**
     * 私有方法，用于检查文件夹名称是否在同一组织内的父文件夹下是唯一的。
     *

     */
    private Mono<Void> checkFolderNameUnique(@Nullable String parentFolderId, String name, String orgId) {
        // 获取同一组织内的所有文件夹
        return folderService.findByOrganizationId(orgId)
                // 过滤出与指定父文件夹ID匹配的文件夹
                .filter(folder -> StringUtils.equals(parentFolderId, folder.getParentFolderId()))
                // 获取所有匹配文件夹的名称
                .map(Folder::getName)
                .collectList()
                // 收集所有匹配文件夹的名称并进行检查
                .flatMap(list -> {
                    // 如果列表中包含与指定名称相同的名称，则返回包含BizException的Mono
                    if (list.contains(name)) {
                        return Mono.error(new BizException(BizError.FOLDER_NAME_CONFLICT, "FOLDER_NAME_CONFLICT"));
                    }
                    // 如果列表中不包含与指定名称相同的名称，则返回空的Mono
                    return Mono.empty();
                });
    }

    /**
     * 删除指定文件夹的服务。
     *
     * 只有组织管理员和文件夹创建者可以删除文件夹。
     * 当文件夹被删除时，所有子文件夹也将被删除，所有子文件将被移动到根文件夹中。
     *

     */
    public Mono<Folder> delete(@Nonnull String folderId) {
        // 1. 检查是否有管理权限
        return checkManagePermission(folderId)
                // 2. 构建文件夹树
                .flatMap(orgMember -> buildFolderTree(orgMember.getOrgId()))
                // 3. 删除文件夹及其子文件夹和子文件
                .flatMap(tree -> {
                    // 获取要删除的文件夹节点
                    FolderNode<Object, Folder> folderNode = tree.get(folderId);
                    if (folderNode == null) {
                        // 如果文件夹不存在，返回包含BizException的Mono
                        return Mono.error(new BizException(FOLDER_NOT_EXIST, "FOLDER_NOT_EXIST", folderId));
                    }
                    // 获取所有要删除的子文件夹ID
                    @SuppressWarnings("ConstantConditions")
                    List<String> folderIds = folderNode.getAllFolderChildren().stream().map(Folder::getId).toList();
                    // 所有要删除的ID列表
                    List<String> all = new ArrayList<>(folderIds);
                    all.add(folderId);

                    // 删除文件夹及其子文件夹
                    return folderService.deleteAllById(all)
                            // 删除文件夹的权限
                            .then(removePermissions(folderId))
                            // 删除文件夹元素关系
                            .then(folderElementRelationService.deleteByFolderIds(all))
                            // 返回被删除的文件夹
                            .thenReturn(folderNode.getSelf());
                });
    }

    /**
     * 私有方法，用于删除指定文件夹的所有权限。
     *
     * @param folderId 要删除权限的目标文件夹ID。
     * @return 空的Mono，表示操作完成。
     *

     */
    private Mono<Void> removePermissions(String folderId) {
        // 获取与指定文件夹ID匹配的资源权限
        return resourcePermissionService.getByResourceTypeAndResourceId(ResourceType.FOLDER, folderId)
                // 迭代获取到的所有资源权限
                .flatMapIterable(Function.identity())
                // 删除每一个资源权限
                .flatMap(resourcePermission -> resourcePermissionService.removeById(resourcePermission.getId()))
                // 最后返回空的Mono，表示操作完成
                .then();
    }

    /**
     * 更新文件夹的服务。
     *
     * @param folder 要更新的目标文件夹。
     * @return 包含更新后的文件夹信息视图的Mono。
     *

     */
    public Mono<FolderInfoView> update(Folder folder) {
        // 创建一个新的文件夹对象，用于更新
        Folder newFolder = new Folder();
        newFolder.setName(folder.getName());

        // 1. 检查是否有管理权限
        return checkManagePermission(folder.getId())
                // 2. 更新文件夹
                .then(folderService.updateById(folder.getId(), newFolder))
                // 3. 获取更新后的文件夹
                .then(folderService.findById(folder.getId()))
                // 4. 构建文件夹信息视图
                .flatMap(f -> buildFolderInfoView(f, true, true));
    }

    /**
     * 移动应用的服务。
     *
     * @param applicationLikeId 要移动的应用的ID。
     * @param targetFolderId 要移动到的目标文件夹的ID。如果为null，表示移动到根文件夹。
     *

     */
    public Mono<Void> move(String applicationLikeId, @Nullable String targetFolderId) {
        // 获取访客ID
        return sessionUserService.getVisitorId()
                // 检查权限
                .delayUntil(userId -> resourcePermissionService.checkResourcePermissionWithError(userId, applicationLikeId,
                        ResourceAction.MANAGE_APPLICATIONS))
                // 删除旧的关联关系
                .then(folderElementRelationService.deleteByElementId(applicationLikeId))
                // 创建新的关联关系
                .flatMap(b -> {
                    if (StringUtils.isBlank(targetFolderId)) {
                        // 如果目标文件夹ID为空，则返回空的Mono
                        return Mono.empty();
                    }
                    // 创建新的关联关系
                    return folderElementRelationService.create(targetFolderId, applicationLikeId);
                })
                // 最后返回空的Mono，表示操作完成
                .then();
    }

    /**
     * 更新用户对文件夹的最后查看时间的服务。
     *
     * @param folderId 要更新的目标文件夹的ID。如果为null，则不进行任何操作。
     *

     */
    public Mono<Void> upsertLastViewTime(@Nullable String folderId) {
        // 如果文件夹ID为空，则返回空的Mono，表示不进行任何操作
        if (StringUtils.isBlank(folderId)) {
            return Mono.empty();
        }

        // 获取访客ID
        return sessionUserService.getVisitorId()
                // 然后使用访客ID和文件夹ID来更新用户与文件夹的交互记录
                .flatMap(userId -> userFolderInteractionService.upsert(userId, folderId, Instant.now()));
    }

    /**
     * 获取文件夹或根目录的子元素的服务。
     *
     * @param folderId 要获取子元素的目标文件夹的ID。如果为null，则表示获取根目录的子元素。
     * @param applicationType 要获取的应用类型。如果为null，则表示获取所有类型的应用。
     *
     * @return 包含 {@link ApplicationInfoView} 或 {@link FolderInfoView} 的 Flux。
     *

     */
    public Flux<?> getElements(@Nullable String folderId, @Nullable ApplicationType applicationType) {
        // 1. 构建应用信息视图树
        return buildApplicationInfoViewTree(applicationType)
                // 2. 获取指定文件夹节点
                .flatMap(tree -> {
                    FolderNode<ApplicationInfoView, FolderInfoView> folderNode = tree.get(folderId);
                    if (folderNode == null) {
                        // 如果文件夹不存在，返回包含BizException的Mono
                        return Mono.error(new BizException(FOLDER_NOT_EXIST, "FOLDER_NOT_EXIST", folderId));
                    }
                    // 如果文件夹存在，返回包含文件夹节点的Mono
                    return Mono.just(folderNode);
                })
                // 3. 并行获取访客的组织成员信息和检查当前组织是否为开发者
                .zipWith(Mono.zip(sessionUserService.getVisitorOrgMemberCache(), orgDevChecker.isCurrentOrgDev()))
                // 4. 处理文件夹节点并设置可见性和管理权限
                .doOnNext(tuple -> {
                    FolderNode<ApplicationInfoView, FolderInfoView> node = tuple.getT1();
                    OrgMember orgMember = tuple.getT2().getT1();
                    boolean devOrAdmin = tuple.getT2().getT2();
                    // 子文件夹的可见性取决于父文件夹的子节点
                    node.postOrderIterate(n -> {
                        if (n instanceof FolderNode<ApplicationInfoView, FolderInfoView> folderNode) {
                            FolderInfoView folderInfoView = folderNode.getSelf();
                            if (folderInfoView == null) {
                                return;
                            }
                            // 设置文件夹的管理权限
                            folderInfoView.setManageable(orgMember.isAdmin() || orgMember.getUserId().equals(folderInfoView.getCreateBy()));

                            // 获取子文件夹和应用
                            List<FolderInfoView> folderInfoViews = folderNode.getFolderChildren().stream().filter(FolderInfoView::isVisible).toList();
                            folderInfoView.setSubFolders(folderInfoViews);
                            folderInfoView.setSubApplications(folderNode.getElementChildren());

                            // 设置文件夹的可见性
                            folderInfoView.setVisible(devOrAdmin || isNotEmpty(folderInfoViews) || isNotEmpty(folderInfoView.getSubApplications()));
                        }
                    });
                })
                // 5. 迭代获取所有子节点并返回
                .flatMapIterable(tuple -> tuple.getT1().getChildren())
                .map(node -> {
                    // 如果是应用节点，返回应用信息视图
                    if (node instanceof ElementNode<ApplicationInfoView, FolderInfoView> elementNode) {
                        return elementNode.getSelf();
                    }
                    // 如果是文件夹节点，返回文件夹信息视图
                    return ((FolderNode<ApplicationInfoView, FolderInfoView>) node).getSelf();
                });
    }

    /**
     * 私有方法，用于构建文件夹树的服务。
     *
     * @param orgId 要构建文件夹树的目标组织的ID。
     *
     * @return 包含 {@link Tree} 的 Mono，其中泛型参数为 {@link Object} 和 {@link Folder}。
     *

     */
    private Mono<Tree<Object, Folder>> buildFolderTree(String orgId) {
        // 获取指定组织下的所有文件夹
        return folderService.findByOrganizationId(orgId)
                // 收集所有文件夹并转换为列表
                .collectList()
                // 使用列表构建文件夹树
                .map(folders -> new Tree<>(folders, Folder::getId, Folder::getParentFolderId, Collections.emptyList(), null, null));
    }

    /**
     * 私有方法，用于构建应用信息视图树的服务。
     *
     * @param applicationType 要构建应用信息视图树的应用类型。如果为null，则表示构建所有类型的应用。
     *
     * @return 包含 {@link Tree} 的 Mono，其中泛型参数为 {@link ApplicationInfoView} 和 {@link FolderInfoView}。
     *

     */
    private Mono<Tree<ApplicationInfoView, FolderInfoView>> buildApplicationInfoViewTree(@Nullable ApplicationType applicationType) {
        // 获取访客的组织成员信息
        Mono<OrgMember> orgMemberMono = sessionUserService.getVisitorOrgMemberCache()
                .cache();
        // 获取所有授权的应用
        Flux<ApplicationInfoView> applicationInfoViewFlux =
                userHomeApiService.getAllAuthorisedApplications4CurrentOrgMember(applicationType, ApplicationStatus.NORMAL, false)
                        .cache();
        // 获取应用与文件夹的映射关系
        Mono<Map<String, String>> application2FolderMapMono = applicationInfoViewFlux
                .map(ApplicationInfoView::getApplicationId)
                .collectList()
                .flatMapMany(applicationIds -> folderElementRelationService.getByElementIds(applicationIds))
                .collectMap(FolderElement::elementId, FolderElement::folderId);
        // 获取所有文件夹
        Flux<Folder> folderFlux = orgMemberMono.flatMapMany(orgMember -> folderService.findByOrganizationId(orgMember.getOrgId()))
                .cache();
        // 获取文件夹与最后查看时间的映射关系
        Mono<Map<String, Instant>> folderId2LastViewTimeMapMono = orgMemberMono
                .flatMapMany(orgMember -> userFolderInteractionService.findByUserId(orgMember.getUserId()))
                .collectMap(UserFolderInteraction::folderId, UserFolderInteraction::lastViewTime)
                .cache();
        // 获取文件夹的创建者信息
        Mono<Map<String, User>> userMapMono = folderFlux
                .flatMap(folder -> emptyIfNull(folder.getCreatedBy()))
                .collectList()
                .flatMap(list -> userService.getByIds(list))
                .cache();
        // 构建文件夹信息视图
        Flux<FolderInfoView> folderInfoViewFlux = folderFlux
                .flatMap(folder -> Mono.zip(orgMemberMono, userMapMono, folderId2LastViewTimeMapMono)
                        .map(tuple -> {
                            OrgMember orgMember = tuple.getT1();
                            Map<String, User> userMap = tuple.getT2();
                            Map<String, Instant> folderId2LastViewTimeMap = tuple.getT3();
                            User creator = userMap.get(folder.getCreatedBy());
                            return FolderInfoView.builder()
                                    .orgId(orgMember.getOrgId())
                                    .folderId(folder.getId())
                                    .parentFolderId(folder.getParentFolderId())
                                    .name(folder.getName())
                                    .createAt(folder.getCreatedAt().toEpochMilli())
                                    .createBy(creator == null ? null : creator.getName())
                                    .createTime(folder.getCreatedAt())
                                    .lastViewTime(folderId2LastViewTimeMap.get(folder.getId()))
                                    .build();
                        }));
        // 构建应用信息视图树
        return Mono.zip(applicationInfoViewFlux.collectList(),
                        application2FolderMapMono,
                        folderInfoViewFlux.collectList())
                .map(tuple -> {
                    List<ApplicationInfoView> applicationInfoViews = tuple.getT1();
                    Map<String, String> application2FolderMap = tuple.getT2();
                    List<FolderInfoView> folderInfoViews = tuple.getT3();
                    return new Tree<>(folderInfoViews,
                            FolderInfoView::getFolderId,
                            FolderInfoView::getParentFolderId,
                            applicationInfoViews,
                            application -> application2FolderMap.get(application.getApplicationId()),
                            DEFAULT_COMPARATOR);
                });
    }

    /**
     * 私有方法，用于检查管理权限的服务。
     * 只有组织管理员和文件夹创建者才有管理权限。
     *
     * @param folderId 要检查管理权限的目标文件夹的ID。
     * @return 包含 {@link OrgMember} 的 Mono。

     */
    private Mono<OrgMember> checkManagePermission(String folderId) {
        // 获取访客的组织成员信息
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    // 如果是组织管理员，返回包含组织成员信息的Mono
                    if (orgMember.isAdmin()) {
                        return Mono.just(orgMember);
                    }
                    // 检查访客是否是文件夹的创建者
                    return isCreator(folderId)
                            .flatMap(isCreator -> isCreator ? Mono.just(orgMember)
                                                            // 如果不是文件夹的创建者，返回包含BizException的Mono
                                                            : ofError(FOLDER_OPERATE_NO_PERMISSION, "FOLDER_OPERATE_NO_PERMISSION"));
                });
    }

    /**
     * 私有方法，用于检查访客是否是文件夹的创建者的服务。
     *
     * @param folderId 要检查的目标文件夹的ID。
     *
     * @return 包含 {@link Boolean} 的 Mono，表示访客是否是文件夹的创建者。
     *

     */
    private Mono<Boolean> isCreator(String folderId) {
        return folderService.findById(folderId)
                .flatMap(folder -> sessionUserService.getVisitorId().map(s -> s.equals(folder.getCreatedBy())));
    }
    /**
     * 公共方法，用于授予文件夹的权限的服务。
     *
     * @param folderId 要授予权限的目标文件夹的ID。
     * @param userIds 要授予权限的用户ID集合。
     * @param groupIds 要授予权限的组ID集合。
     * @param role 要授予的权限角色。
     *
     * @return 包含 {@link Void} 的 Mono，表示操作完成。
     *

     */
    public Mono<Void> grantPermission(String folderId, Set<String> userIds, Set<String> groupIds, ResourceRole role) {
        if (CollectionUtils.isEmpty(userIds) && CollectionUtils.isEmpty(groupIds)) {
            return Mono.empty();
        }
        return Mono.from(checkManagePermission(folderId))
                .then(checkFolderExist(folderId))
                .then(Mono.defer(() -> resourcePermissionService.insertBatchPermission(ResourceType.FOLDER, folderId, userIds, groupIds, role)))
                .then();
    }

    /**
     * 公共方法，用于更新文件夹的权限的服务。
     *
     * @param folderId 要更新权限的目标文件夹的ID。
     * @param permissionId 要更新的权限的ID。
     * @param role 要更新的权限角色。
     *
     * @return 包含 {@link Void} 的 Mono，表示操作完成。
     *

     */
    public Mono<Void> updatePermission(String folderId, String permissionId, ResourceRole role) {
        return Mono.from(checkManagePermission(folderId))
                .then(checkPermissionResource(permissionId, folderId))
                .then(resourcePermissionService.updateRoleById(permissionId, role))
                .then();
    }

    /**
     * 公共方法，用于删除文件夹的权限的服务。
     *
     * @param folderId 要删除权限的目标文件夹的ID。
     * @param permissionId 要删除的权限的ID。
     *
     * @return 包含 {@link Void} 的 Mono，表示操作完成。
     *

     */
    public Mono<Void> removePermission(String folderId, String permissionId) {
        return Mono.from(checkManagePermission(folderId))
                .then(checkPermissionResource(permissionId, folderId))
                .then(resourcePermissionService.removeById(permissionId))
                .then();
    }
    /**
     * 私有方法，用于检查权限资源的服务。
     *
     * @param permissionId 要检查的权限的ID。
     * @param folderId 要检查的目标文件夹的ID。
     *
     * @return 包含 {@link Void} 的 Mono，表示操作完成。
     *

     */
    private Mono<Void> checkPermissionResource(String permissionId, String folderId) {
        return resourcePermissionService.getById(permissionId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BizException(ILLEGAL_FOLDER_PERMISSION_ID, "PERMISSION_NOT_EXIST"))))
                .flatMap(resourcePermission -> {
                    if (!folderId.equals(resourcePermission.getResourceId())) {
                        return Mono.error(new BizException(ILLEGAL_FOLDER_PERMISSION_ID, "NO_PERMISSION_TO_OPERATE_FOLDER"));
                    }
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 公共方法，用于获取文件夹的权限的服务。
     *
     * @param folderId 要获取权限的目标文件夹的ID。
     *
     * @return 包含 {@link ApplicationPermissionView} 的 Mono，表示文件夹的权限信息。
     *

     */
    public Mono<ApplicationPermissionView> getPermissions(String folderId) {
        // 获取文件夹的权限
        Mono<List<ResourcePermission>> folderPermissions =
                resourcePermissionService.getByResourceTypeAndResourceId(ResourceType.FOLDER, folderId).cache();
        // 获取文件夹的组权限
        Mono<List<PermissionItemView>> groupPermissionPairsMono = folderPermissions
                .flatMap(permissionHelper::getGroupPermissions);
        // 获取文件夹的用户权限
        Mono<List<PermissionItemView>> userPermissionPairsMono = folderPermissions
                .flatMap(permissionHelper::getUserPermissions);
        // 获取文件夹信息并构建ApplicationPermissionView
        return folderService.findById(folderId)
                .flatMap(folder -> {
                    Mono<Organization> orgMono = organizationService.getById(folder.getOrganizationId());
                    return Mono.zip(groupPermissionPairsMono, userPermissionPairsMono, orgMono)
                            .map(tuple -> {
                                List<PermissionItemView> groupPermissionPairs = tuple.getT1();
                                List<PermissionItemView> userPermissionPairs = tuple.getT2();
                                Organization organization = tuple.getT3();
                                return ApplicationPermissionView.builder()
                                        .groupPermissions(groupPermissionPairs)
                                        .userPermissions(userPermissionPairs)
                                        .creatorId(folder.getCreatedBy())
                                        .orgName(organization.getName())
                                        .build();
                            });
                });
    }

    /**
     * 公共方法，用于构建文件夹信息视图的服务。
     *
     * @param folder 要构建信息视图的目标文件夹。
     * @param visible 指示文件夹是否对当前用户可见。
     * @param manageable 指示文件夹是否对当前用户可管理。
     *
     * @return 包含 {@link FolderInfoView} 的 Mono，表示文件夹信息视图。
     *

     */
    public Mono<FolderInfoView> buildFolderInfoView(Folder folder, boolean visible, boolean manageable) {
        return userService.findById(folder.getCreatedBy())
                .map(user -> FolderInfoView.builder()
                        .orgId(folder.getOrganizationId())
                        .folderId(folder.getId())
                        .parentFolderId(folder.getParentFolderId())
                        .name(folder.getName())
                        .createAt(folder.getCreatedAt() == null ? 0 : folder.getCreatedAt().toEpochMilli())
                        .createBy(user.getName())
                        .createTime(folder.getCreatedAt())
                        .isVisible(visible)
                        .isManageable(manageable)
                        .build());
    }
}
