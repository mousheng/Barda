package com.barda.infra.birelation;

/**
 * 枚举类表示双向关系业务类型。
 */
public enum BiRelationBizType {

    /**
     * 表示组织成员关系的业务类型。
     */
    ORG_MEMBER,

    /**
     * 表示群组成员关系的业务类型。
     */
    GROUP_MEMBER,

    /**
     * 表示资源关系的业务类型。
     */
    RESOURCE,

    /**
     * 表示文件夹元素关系的业务类型。
     */
    FOLDER_ELEMENT,

    /**
     * 表示用户应用交互关系的业务类型。
     */
    USER_APP_INTERACTION,

    /**
     * 表示用户文件夹交互关系的业务类型。
     */
    USER_FOLDER_INTERACTION,
}

