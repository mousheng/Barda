package com.barda.domain.query.model;

import org.apache.commons.lang.StringUtils;

/**
 * 库查询的复合ID表示。
 * 它是一个记录类，使用Java 14的记录功能来表示复合ID。
 * 该类包含库查询ID和库查询记录ID的字段。
 */
public record LibraryQueryCombineId(String libraryQueryId, String libraryQueryRecordId) {

    /**
     * 检查是否使用库查询的最新记录。
     *
     * @return 如果库查询记录ID为"latest"，则返回true；否则返回false
     */
    public boolean isUsingLiveRecord() {
        return "latest".equals(libraryQueryRecordId);
    }

    /**
     * 检查是否使用库查询的编辑中记录。
     *
     * @return 如果库查询记录ID为空或为"editing"，则返回true；否则返回false
     */
    public boolean isUsingEditingRecord() {
        return StringUtils.isBlank(libraryQueryRecordId) || "editing".equals(libraryQueryRecordId);
    }
}
