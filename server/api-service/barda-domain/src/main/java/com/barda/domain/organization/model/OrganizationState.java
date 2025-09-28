package com.barda.domain.organization.model;

/**
 * 组织状态枚举。
 * 该枚举定义了组织可以拥有的不同状态。
 */
public enum OrganizationState {

    /**
     * 活动状态。
     * 指示该组织处于活动状态，可以正常使用。
     */
    ACTIVE,

    /**
     * 删除状态。
     * 指示该组织已被删除，不再处于活动状态。
     */
    DELETED
}
