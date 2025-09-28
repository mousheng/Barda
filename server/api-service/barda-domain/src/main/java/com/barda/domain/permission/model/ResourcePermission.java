package com.barda.domain.permission.model;

import static com.barda.domain.permission.config.PermissionConst.ID_SPLITTER;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;
import com.barda.infra.birelation.BiRelation;
import com.barda.infra.birelation.BiRelationBizType;
import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Builder;
import lombok.Getter;

/**
 * 资源权限类。
 * 该类表示对特定资源的访问权限，并提供方法来检查用户是否有权执行特定操作。
 */
@Getter
@Builder
public class ResourcePermission extends HasIdAndAuditing {

    /**
     * 资源类型。
     */
    private ResourceType resourceType;

    /**
     * 资源 ID。
     */
    private String resourceId;

    /**
     * 资源持有者类型。
     */
    private ResourceHolder resourceHolder;

    /**
     * 资源持有者 ID。
     */
    private String resourceHolderId;

    /**
     * 资源角色。
     */
    private ResourceRole resourceRole;

    /**
     * 检查用户是否有权执行特定操作。
     *
     * @param userId 用户 ID
     * @param resourceAction 资源操作
     * @return true 如果用户有权执行操作，否则返回 false
     */
    public boolean matchUser(String userId, ResourceAction resourceAction) {
        return resourceHolder == ResourceHolder.USER
                && StringUtils.equals(userId, resourceHolderId)
                && resourceRole.canDo(resourceAction);
    }

    /**
     * 检查群组是否有权执行特定操作。
     *
     * @param userGroupIds 用户所属的群组 ID 集合
     * @param resourceAction 资源操作
     * @return true 如果群组有权执行操作，否则返回 false
     */
    public boolean matchGroup(Set<String> userGroupIds, ResourceAction resourceAction) {
        return resourceHolder == ResourceHolder.GROUP
                && userGroupIds.contains(resourceHolderId)
                && resourceRole.canDo(resourceAction);
    }

    /**
     * 检查资源是否由群组拥有。
     *
     * @return true 如果资源由群组拥有，否则返回 false
     */
    public boolean ownedByGroup() {
        return resourceHolder == ResourceHolder.GROUP;
    }

    /**
     * 检查资源是否由用户拥有。
     *
     * @return true 如果资源由用户拥有，否则返回 false
     */
    public boolean ownedByUser() {
        return resourceHolder == ResourceHolder.USER;
    }

    /**
     * 从连接的字符串中解析 ID。
     *
     * @param joinedStr 连接的字符串
     * @return 解析出的 ID
     */
    public static String parseId(String joinedStr) {
        return StringUtils.substringAfter(joinedStr, ID_SPLITTER);
    }

    /**
     * 从双向关系中创建 ResourcePermission 实例。
     *
     * @param biRelation 双向关系
     * @return 创建的 ResourcePermission 实例
     */
    public static ResourcePermission fromBiRelation(BiRelation biRelation) {
        String sourceId = biRelation.getSourceId();
        List<String> sourceStrings = Splitter.on(ID_SPLITTER).splitToList(sourceId);
        String resourceId = sourceStrings.get(1);
        ResourceType resourceType = ResourceType.from(sourceStrings.get(0));

        String targetId = biRelation.getTargetId();
        List<String> targetStrings = Splitter.on(ID_SPLITTER).splitToList(targetId);
        ResourceHolder holderType = ResourceHolder.from(targetStrings.get(0));
        String holderId = targetStrings.get(1);

        ResourceRole role = ResourceRole.fromValue(biRelation.getRelation());

        ResourcePermission permission = ResourcePermission.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .resourceHolder(holderType)
                .resourceHolderId(holderId)
                .resourceRole(role)
                .build();
        permission.setId(biRelation.getId());
        return permission;
    }

    /**
     * 将 ResourcePermission 实例转换为双向关系。
     *
     * @return 创建的 BiRelation 实例
     */
    public BiRelation toBiRelation() {
        return BiRelation.builder()
                .bizType(BiRelationBizType.RESOURCE)
                .sourceId(resourceType.join(resourceId))
                .targetId(resourceHolder.join(resourceHolderId))
                .relation(resourceRole == null ? "" : resourceRole.getValue())
                .build();
    }
}
