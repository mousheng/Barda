package com.barda.api.application;

import static com.barda.domain.permission.model.ResourceAction.READ_APPLICATIONS;
import static com.barda.sdk.constants.DslConstants.CompoundAppDslConstants.ACTION;
import static com.barda.sdk.constants.DslConstants.CompoundAppDslConstants.APP;
import static com.barda.sdk.constants.DslConstants.CompoundAppDslConstants.APP_ID;
import static com.barda.sdk.constants.DslConstants.CompoundAppDslConstants.COMP;
import static com.barda.sdk.constants.DslConstants.CompoundAppDslConstants.HIDE_WHEN_NO_PERMISSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.barda.api.home.SessionUserService;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.sdk.constants.DslConstants.CompoundAppDslConstants;
import com.barda.sdk.util.MoreMapUtils;

import reactor.core.publisher.Mono;

/**
 * 用于复合应用的DSL过滤器类，从复合应用的DSL中移除其子应用，在其上当前用户没有权限的子应用。
 */
@Component
public class CompoundApplicationDslFilter {

    /**
     * 会话用户服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 资源权限服务。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 从复合应用的DSL中移除其子应用，在其上当前用户没有权限的子应用。
     *
     * @param dsl 复合应用的DSL。
     * @return 一个Mono，表示操作完成。
     */
    @SuppressWarnings("unchecked")
    public Mono<Void> removeSubAppsFromCompoundDsl(Map<String, Object> dsl) {
        Map<String, Object> ui = (Map<String, Object>) MapUtils.getMap(dsl, CompoundAppDslConstants.UI, new HashMap<>());
        Map<String, Object> comp = (Map<String, Object>) MapUtils.getMap(ui, CompoundAppDslConstants.COMP, new HashMap<>());

        Set<String> subApplicationIds = getAllSubAppIdsFromCompoundAppDsl(comp);
        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> resourcePermissionService.getMaxMatchingPermission(visitorId, subApplicationIds, READ_APPLICATIONS))
                .map(Map::keySet)
                .map(applicationIdsWithPermissions -> Sets.difference(subApplicationIds, applicationIdsWithPermissions))
                .doOnNext(applicationIdsWithoutPermissions -> removeSubAppsFromCompoundDsl(comp, applicationIdsWithoutPermissions))
                .then();
    }

    /**
     * 从复合 DSL 中移除子应用程序。
     *
     * @param dsl              复合 DSL
     * @param appIdsNeedRemoved 需要移除的应用程序ID集合
     */
    private void removeSubAppsFromCompoundDsl(Map<String, Object> dsl, Set<String> appIdsNeedRemoved) {

        List<Map<String, Object>> items = MoreMapUtils.getList(dsl, CompoundAppDslConstants.ITEMS, new ArrayList<>());
        Iterator<Map<String, Object>> iterator = items.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> item = iterator.next();
            // 处理叶子节点且其子节点为空的情况
            if (isLeaf(item)) {
                // 如果没有权限则跳过
                if (!hideWhenNoPermission(item)) {
                    continue;
                }

                String appId = getAppId(item);
                if (StringUtils.isNotBlank(appId) && appIdsNeedRemoved.contains(appId)) {
                    iterator.remove();
                    continue;
                }
                continue;
            }

            // 处理非叶子节点
            // 递归处理
            removeSubAppsFromCompoundDsl(item, appIdsNeedRemoved);
            // 在移除有条件的子应用程序后，非叶子节点可能变为叶子节点，此时需要将其自身移除
            if (isLeaf(item)) {
                iterator.remove();
            }
        }
    }

    private boolean isLeaf(Map<String, Object> item) {
        List<Map<String, Object>> subItems = MoreMapUtils.getList(item, CompoundAppDslConstants.ITEMS, new ArrayList<>());
        return CollectionUtils.isEmpty(subItems);
    }

    /**
     * 从复合应用的DSL中递归地找出所有子应用ID。
     *
     * @param dsl 复合应用的DSL。
     * @return 包含所有子应用ID的集合。
     */
    public Set<String> getAllSubAppIdsFromCompoundAppDsl(Map<String, Object> dsl) {
        List<Map<String, Object>> items = MoreMapUtils.getList(dsl, CompoundAppDslConstants.ITEMS, new ArrayList<>());
        return items.stream()
                .map(item -> {
                    // If the item is a leaf node, find its id and return it.
                    if (isLeaf(item)) {
                        String appId = getAppId(item);
                        if (StringUtils.isBlank(appId)) {
                            return Collections.<String> emptySet();
                        }
                        return Collections.singleton(appId);
                    }
                    // If the item is a non-leaf node, find sub-application ids recursively and return them.
                    return getAllSubAppIdsFromCompoundAppDsl(item);
                })
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * 获取应用ID。
     *
     * @param item 包含应用ID的项。
     * @return 应用ID。
     */
    @SuppressWarnings("unchecked")
    private String getAppId(Map<String, Object> item) {
        return Optional.ofNullable((Map<String, Object>) MapUtils.getMap(item, ACTION))
                .map(action -> (Map<String, Object>) MapUtils.getMap(action, COMP))
                .or(() -> Optional.of(item)) // compatible code
                .map(i -> (Map<String, Object>) MapUtils.getMap(i, APP))
                .map(app -> MapUtils.getString(app, APP_ID))
                .orElse(null);
    }

    /**
     * 获取是否在没有权限时隐藏的标志。
     *
     * @param item 包含隐藏标志的项。
     * @return 布尔值，表示是否在没有权限时隐藏。
     */
    @SuppressWarnings("unchecked")
    private boolean hideWhenNoPermission(Map<String, Object> item) {
        return Optional.ofNullable((Map<String, Object>) MapUtils.getMap(item, ACTION))
                .map(action -> (Map<String, Object>) MapUtils.getMap(action, COMP))
                .or(() -> Optional.of(item)) // compatible code
                .map(i -> MapUtils.getBoolean(i, HIDE_WHEN_NO_PERMISSION))
                .orElse(true);
    }
}