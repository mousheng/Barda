package com.barda.api.query.view;

import com.barda.domain.query.model.LibraryQuery;
import com.barda.domain.user.model.User;

/**
 * 库查询元数据视图类。
 * 该类用于封装库查询的元数据，并提供从领域模型创建视图的静态方法。
 */
public record LibraryQueryMetaView(String id,
                                   String datasourceType,
                                   String organizationId,
                                   String name,
                                   long createTime,
                                   String creatorName) {

    /**
     * 从领域模型创建库查询元数据视图。
     *
     * @param libraryQuery 库查询
     * @param user 库查询创建者
     * @return 库查询元数据视图
     */
    public static LibraryQueryMetaView from(LibraryQuery libraryQuery, User user) {
        return new LibraryQueryMetaView(libraryQuery.getId(),
                libraryQuery.getQuery().getCompType(),
                libraryQuery.getOrganizationId(),
                libraryQuery.getName(),
                libraryQuery.getCreatedAt().toEpochMilli(),
                user.getName());
    }
}
