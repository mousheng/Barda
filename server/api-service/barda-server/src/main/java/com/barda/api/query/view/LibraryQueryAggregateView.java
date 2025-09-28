package com.barda.api.query.view;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.barda.domain.query.model.LibraryQuery;
import com.barda.domain.query.model.LibraryQueryRecord;
import com.barda.domain.user.model.User;

/**
 * 库查询聚合视图类。
 * 该类用于封装库查询的元数据和记录元数据，并提供从领域模型创建视图的静态方法。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LibraryQueryAggregateView(LibraryQueryMetaView libraryQueryMetaView,
                                        List<LibraryQueryRecordMetaView> recordMetaViewList) {

    /**
     * 从领域模型创建库查询聚合视图。
     *
     * @param libraryQuery 库查询
     * @param libraryQueryCreator 库查询创建者
     * @param libraryQueryRecordList 库查询记录列表
     * @return 库查询聚合视图
     */
    public static LibraryQueryAggregateView from(LibraryQuery libraryQuery, User libraryQueryCreator, List<LibraryQueryRecord> libraryQueryRecordList) {
        List<LibraryQueryRecordMetaView> libraryQueryRecordMetaViews = libraryQueryRecordList.stream()
                .map(LibraryQueryRecordMetaView::from)
                .toList();
        LibraryQueryMetaView libraryQueryMetaView = LibraryQueryMetaView.from(libraryQuery, libraryQueryCreator);
        return new LibraryQueryAggregateView(libraryQueryMetaView, libraryQueryRecordMetaViews);
    }

    /**
     * 从领域模型创建库查询聚合视图（不包含记录）。
     *
     * @param libraryQuery 库查询
     * @param libraryQueryCreator 库查询创建者
     * @return 库查询聚合视图
     */
    public static LibraryQueryAggregateView from(LibraryQuery libraryQuery, User libraryQueryCreator) {
        LibraryQueryMetaView libraryQueryMetaView = LibraryQueryMetaView.from(libraryQuery, libraryQueryCreator);
        return new LibraryQueryAggregateView(libraryQueryMetaView, null);
    }
}
