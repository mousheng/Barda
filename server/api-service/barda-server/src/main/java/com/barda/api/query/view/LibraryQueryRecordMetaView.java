package com.barda.api.query.view;

import com.barda.domain.query.model.LibraryQueryRecord;
import com.barda.domain.user.model.User;

/**
 * 库查询记录元视图。
 * 该类用于封装库查询记录的元数据，并提供便捷的构造器方法。
 */
public record LibraryQueryRecordMetaView(String id,
                                         String libraryQueryId,
                                         String datasourceType,
                                         String tag,
                                         String commitMessage,
                                         long createTime,
                                         String creatorName) {

    /**
     * 从库查询记录中创建一个新的 {@link LibraryQueryRecordMetaView} 实例。
     *
     * @param libraryQueryRecord 库查询记录
     * @return 库查询记录元视图
     */
    public static LibraryQueryRecordMetaView from(LibraryQueryRecord libraryQueryRecord) {
        return new LibraryQueryRecordMetaView(libraryQueryRecord.getId(),
                libraryQueryRecord.getLibraryQueryId(),
                libraryQueryRecord.getQuery().getCompType(),
                libraryQueryRecord.getTag(),
                libraryQueryRecord.getCommitMessage(),
                libraryQueryRecord.getCreatedAt().toEpochMilli(),
                null);
    }

    /**
     * 从库查询记录和创建者中创建一个新的 {@link LibraryQueryRecordMetaView} 实例。
     *
     * @param libraryQueryRecord 库查询记录
     * @param libraryQueryRecordCreator 库查询记录创建者
     * @return 库查询记录元视图
     */
    public static LibraryQueryRecordMetaView from(LibraryQueryRecord libraryQueryRecord, User libraryQueryRecordCreator) {
        return new LibraryQueryRecordMetaView(libraryQueryRecord.getId(),
                libraryQueryRecord.getLibraryQueryId(),
                libraryQueryRecord.getQuery().getCompType(),
                libraryQueryRecord.getTag(),
                libraryQueryRecord.getCommitMessage(),
                libraryQueryRecord.getCreatedAt().toEpochMilli(),
                libraryQueryRecordCreator.getName());
    }
}
