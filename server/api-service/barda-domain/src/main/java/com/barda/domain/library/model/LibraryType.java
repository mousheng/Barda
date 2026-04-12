package com.barda.domain.library.model;

/**
 * 库文件类型枚举。
 */
public enum LibraryType {

    /**
     * 共享库。
     * 由主要组织管理，所有组织可用。
     */
    SHARED,

    /**
     * 组织私有库。
     * 由组织管理员管理，仅本组织可用。
     */
    ORG
}
