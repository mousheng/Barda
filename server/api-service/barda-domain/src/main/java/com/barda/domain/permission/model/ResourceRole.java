package com.barda.domain.permission.model;

import static com.google.common.collect.Maps.newHashMap;
import static com.barda.sdk.util.StreamUtils.collectMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Streams;

/**
 * 资源角色枚举类。
 * 该类表示可以对特定资源执行的操作的不同角色。
 */
public enum ResourceRole {

    /**
     * 查看者。
     * 具有最低的权限。
     */
    VIEWER("viewer", 1),

    /**
     * 编辑者。
     * 具有比查看者更高的权限。
     */
    EDITOR("editor", 10),

    /**
     * 拥有者。
     * 具有最高的权限。
     */
    OWNER("owner", 100);

    private static final Map<String, ResourceRole> VALUE_MAP;

    static {
        VALUE_MAP = collectMap(Arrays.stream(values()),
                ResourceRole::getValue, Function.identity());
    }

    private final String value;
    private final int roleWeight; // 用于对角色进行排序

    ResourceRole(String value, int roleWeight) {
        this.value = value;
        this.roleWeight = roleWeight;
    }

    /**
     * 从值中获取 ResourceRole 实例。
     *
     * @param role 值
     * @return ResourceRole 实例，如果找不到匹配的值，则返回 null
     */
    @Nullable
    public static ResourceRole fromValue(String role) {
        return VALUE_MAP.get(role);
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    public int getRoleWeight() {
        return roleWeight;
    }

    public boolean canDo(ResourceAction permission) {
        return RolePermissionHelper.canDo(this, permission);
    }

    /**
     * 用于惰性初始化的内部类。
     */
    private static class RolePermissionHelper {

        private static final Map<ResourceRole, Set<ResourceAction>> roleSetMap;

        static {
            Graph<ResourceRole, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
            Arrays.stream(values()).forEach(graph::addVertex);
            graph.addEdge(OWNER, EDITOR);
            graph.addEdge(EDITOR, VIEWER);

            roleSetMap = buildRolePermissionMap(graph, List.of(values()));
        }

        @SuppressWarnings("UnstableApiUsage")
        private static Map<ResourceRole, Set<ResourceAction>> buildRolePermissionMap(Graph<ResourceRole, DefaultEdge> graph,
                Collection<ResourceRole> values) {
            Map<ResourceRole, Set<ResourceAction>> rolePermissionMap = newHashMap();
            for (ResourceRole value : values) {
                var breadthFirstIterator = new BreadthFirstIterator<>(graph, value);
                Set<ResourceAction> collect = Streams.stream(breadthFirstIterator)
                        .map(ResourceAction::getMatchingPermissions)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
                rolePermissionMap.put(value, collect);
            }
            return rolePermissionMap;
        }

        /**
         * 检查角色是否有权执行特定操作。
         *
         * @param role 角色
         * @param permission 操作
         * @return true 如果角色有权执行操作，否则返回 false
         */
        public static boolean canDo(ResourceRole role, ResourceAction permission) {
            return roleSetMap.getOrDefault(role, Collections.emptySet()).contains(permission);
        }
    }
}
