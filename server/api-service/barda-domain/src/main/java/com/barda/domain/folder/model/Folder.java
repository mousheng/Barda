package com.barda.domain.folder.model;

import javax.annotation.Nullable;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Getter;
import lombok.Setter;

/**
 * 该类表示文件夹，并继承了 {@link HasIdAndAuditing} 类，
 * 其中包含了 ID 和审计信息。
 */
@Getter
@Setter
@Document
public class Folder extends HasIdAndAuditing {

    /**
     * 该文件夹所属的组织的 ID。
     */
    private String organizationId;

    /**
     * 该文件夹的父文件夹的 ID。
     * null 值表示该文件夹位于根文件夹中。
     */
    @Nullable
    private String parentFolderId;

    /**
     * 文件夹的名称。
     */
    private String name;
}
