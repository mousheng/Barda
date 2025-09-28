package com.barda.domain.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

/**
 * 应用相关的实用工具类。
 */
public class ApplicationUtil {

    /**
     * 从 DSL 获取容器尺寸。
     *
     * @param dsl DSL 映射
     * @return 容器尺寸，如果在 DSL 中找不到，返回空 Map
     */
    public static Object getContainerSizeFromDSL(Map<String, Object> dsl) {
        Object ui = dsl.get("ui");
        if (!(ui instanceof Map<?, ?> uiMap)) {
            return Collections.emptyMap();
        }
        Object comp = uiMap.get("comp");
        if (!(comp instanceof Map<?, ?> compMap)) {
            return Collections.emptyMap();
        }
        return compMap.get("containerSize");
    }

    /**
     * 从 DSL 获取依赖的模块 ID 集合。
     *
     * @param dsl DSL 映射
     * @return 依赖的模块 ID 集合
     */
    @NotNull
    public static Set<String> getDependentModulesFromDsl(Map<String, Object> dsl) {
        Set<String> dependentModuleIds = Sets.newHashSet();
        doGetDependentModules(dsl, dependentModuleIds);
        return dependentModuleIds;
    }

    /**
     * 递归地从 DSL 获取依赖的模块 ID。
     *
     * @param map DSL 映射
     * @param dependentModuleIds 存储依赖的模块 ID 的集合
     */
    public static void doGetDependentModules(Map<?,?> map, Set<String> dependentModuleIds) {
        Object compType = map.get("compType");
        if (compType instanceof String compTypeStr && compTypeStr.equals("module")) {
            Object comp = map.get("comp");
            if (comp instanceof Map<?, ?> compMap) {
                String appId = (String) compMap.get("appId");
                if (StringUtils.isNotBlank(appId)) {
                    dependentModuleIds.add(appId);
                }
            }
        }

        map.forEach((key, value) -> {
            if (value instanceof Map<?, ?> valueMap) {
                doGetDependentModules(valueMap, dependentModuleIds);
            }
            if (value instanceof List<?> valueList) {
                valueList.forEach(i -> {
                    if (i instanceof Map<?, ?> iMap) {
                        doGetDependentModules(iMap, dependentModuleIds);
                    }
                });
            }
        });
    }
}
