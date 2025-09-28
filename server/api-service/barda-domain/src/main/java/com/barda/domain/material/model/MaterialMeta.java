package com.barda.domain.material.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Builder;
import lombok.Getter;

/**
 * 表示系统中存储的材料的元数据。
 * 该实体映射到一个 MongoDB 文档。
 */
@Getter
@Builder
@Document
public class MaterialMeta extends HasIdAndAuditing {

    /**
     * 文件名。
     */
    private String filename;

    /**
     * 该材料所属的组织的ID。
     */
    private String orgId;

    /**
     * 文件大小，以字节为单位。
     */
    private long size;

    /**
     * 材料的类型。
     */
    private MaterialType type;
}
