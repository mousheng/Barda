package com.barda.api.permission;

import static com.barda.api.util.ViewBuilder.multiBuild;

import java.util.List;
import java.util.Locale;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.api.permission.view.PermissionItemView;
import com.barda.domain.group.model.Group;
import com.barda.domain.group.service.GroupService;
import com.barda.domain.permission.model.ResourceHolder;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.util.LocaleUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 权限帮助类。
 * 该类提供方法来获取组和用户的权限并将其转换为 {@link PermissionItemView} 列表。
 */
@Component
public class PermissionHelper {

    /**
     * 组服务。
     */
    @Autowired
    private GroupService groupService;

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 获取组权限。
     *
     * @param resourcePermissions 资源权限列表
     * @return 组权限列表
     */
    public Mono<List<PermissionItemView>> getGroupPermissions(@NotEmpty List<ResourcePermission> resourcePermissions) {
        return Flux.fromIterable(resourcePermissions)
                .filter(ResourcePermission::ownedByGroup)
                .collectList()
                .flatMap(groupPermissions -> Mono.deferContextual(contextView -> {
                    Locale locale = LocaleUtils.getLocale(contextView);
                    return multiBuild(groupPermissions,
                            ResourcePermission::getResourceHolderId,
                            groupService::getByIds,
                            Group::getId,
                            (permission, group) -> PermissionItemView.builder()
                                    .permissionId(permission.getId())
                                    .type(ResourceHolder.GROUP)
                                    .id(group.getId())
                                    .name(group.getName(locale))
                                    .avatar("")
                                    .role(permission.getResourceRole().getValue())
                                    .build()
                    );
                }));
    }

    /**
     * 获取用户权限。
     *
     * @param resourcePermissions 资源权限列表
     * @return 用户权限列表
     */
    public Mono<List<PermissionItemView>> getUserPermissions(@NotEmpty List<ResourcePermission> resourcePermissions) {
        return Flux.fromIterable(resourcePermissions)
                .filter(ResourcePermission::ownedByUser)
                .collectList()
                .flatMap(userPermissions -> multiBuild(userPermissions,
                        ResourcePermission::getResourceHolderId,
                        userService::getByIds,
                        (permission, user) -> PermissionItemView.builder()
                                .permissionId(permission.getId())
                                .type(ResourceHolder.USER)
                                .id(user.getId())
                                .name(user.getName())
                                .avatar(user.getAvatar())
                                .role(permission.getResourceRole().getValue())
                                .build()
                ));
    }
}
