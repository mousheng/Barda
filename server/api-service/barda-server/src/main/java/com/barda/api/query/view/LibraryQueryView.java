package com.barda.api.query.view;

import java.util.Map;

import com.barda.domain.query.model.LibraryQuery;
import com.barda.domain.user.model.User;

/**
 * 库查询视图类，使用 Java 14 的 record 功能实现。
 *
 * @param id 库查询 ID
 * @param organizationId 所属组织 ID
 * @param name 库查询名称
 * @param libraryQueryDSL 库查询 DSL
 * @param createTime 创建时间（毫秒）
 * @param creatorName 创建者名称
 */
public record LibraryQueryView(String id,
                               String organizationId,
                               String name,
                               Map<String, Object> libraryQueryDSL,
                               long createTime,
                               String creatorName) {

    /**
     * 从 LibraryQuery 对象和 User 对象创建 LibraryQueryView 对象。
     *
     * @param libraryQuery 库查询对象
     * @param user 用户对象
     * @return 库查询视图对象
     */
    public static LibraryQueryView from(LibraryQuery libraryQuery, User user) {
        return new LibraryQueryView(libraryQuery.getId(),
                libraryQuery.getOrganizationId(),
                libraryQuery.getName(),
                libraryQuery.getLibraryQueryDSL(),
                libraryQuery.getCreatedAt().toEpochMilli(),
                user.getName());
    }
}
