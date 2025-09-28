package com.barda.domain.user.model;

/**
 * 用户状态枚举类。
 */
public enum UserState {

    /**
     * 新用户
     */
    NEW,

    /**
     * 已邀请
     */
    INVITED,

    /**
     * 已激活
     */
    ACTIVATED,

    /**
     * 已删除（仅在企业模式下有效）
     */
    DELETED
}
