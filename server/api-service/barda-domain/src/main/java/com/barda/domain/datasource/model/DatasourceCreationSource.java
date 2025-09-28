package com.barda.domain.datasource.model;

import java.util.Arrays;

import lombok.Getter;

/**
 * 该枚举定义了数据源创建来源的类型。
 * 它用于表示数据源是如何创建的，并提供一些相关的操作。
 */
@Getter
public enum DatasourceCreationSource {

    /**
     * 用户创建的数据源。
     */
    USER_CREATED(0),

    /**
     * 从模板克隆的数据源。
     * 例如，在上架数据源/模板数据源时创建，并且用户可以稍后更新它。
     */
    CLONE_FROM_TEMPLATE(1),

    /**
     * 已弃用的来源。
     * 例如，Barda API，在创建工作区时自动创建，无法修改。
     */
    @Deprecated
    LEGACY_WORKSPACE_PREDEFINED(2),

    /**
     * 系统内置的数据源。
     * 例如，REST API/GraphQL 快速数据源，只存在于内存中，不存储在数据库中。
     */
    SYSTEM_STATIC(3),
    ;

    private final int value;

    DatasourceCreationSource(int value) {
        this.value = value;
    }

    /**
     * 根据值返回相应的 {@link DatasourceCreationSource} 枚举值。
     *
     * @param value 值
     * @return 相应的 {@link DatasourceCreationSource} 枚举值
     * @throws IllegalArgumentException 如果找不到匹配的值
     */
    public DatasourceCreationSource fromValue(int value) {
        return Arrays.stream(values())
                .filter(it -> it.getValue() == value)
                .findFirst()
                .orElseThrow();
    }
}
