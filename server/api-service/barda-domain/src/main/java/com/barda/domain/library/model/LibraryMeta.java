package com.barda.domain.library.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 库文件元数据。
 * 用于存储上传的 JavaScript/CSS 库文件的元信息。
 */
@Getter
@Setter
@NoArgsConstructor
@Document
public class LibraryMeta extends HasIdAndAuditing {

    /**
     * 库标识符（用于稳定引用，如 "jquery"）。
     * 从 displayName 生成，全局唯一（同一 type + orgId 下）。
     */
    private String libraryId;

    /**
     * 原始文件名（用户上传时的文件名）。
     */
    private String originalFilename;

    /**
     * 文件名（包含扩展名，存储时使用的文件名，UUID）。
     */
    private String filename;

    /**
     * 显示名称（用户友好的名称）。
     */
    private String displayName;

    /**
     * 版本号。
     */
    private String version;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件类型（SHARED 或 ORG）。
     */
    private LibraryType type;

    /**
     * 组织 ID。
     * - 对于 SHARED 类型，存储主要组织的 ID
     * - 对于 ORG 类型，存储所属组织的 ID
     */
    private String orgId;

    /**
     * 文件的 MIME 类型。
     */
    private String contentType;

    /**
     * 文件描述（可选）。
     */
    private String description;

    @Builder
    public LibraryMeta(String libraryId, String originalFilename, String filename, String displayName, String version, Long fileSize, LibraryType type,
                      String orgId, String contentType, String description) {
        this.libraryId = libraryId;
        this.originalFilename = originalFilename;
        this.filename = filename;
        this.displayName = displayName;
        this.version = version;
        this.fileSize = fileSize;
        this.type = type;
        this.orgId = orgId;
        this.contentType = contentType;
        this.description = description;
    }

    /**
     * 获取在 GridFS 中的存储路径。
     *
     * @return GridFS 存储路径
     */
    public String getGridFsPath() {
        if (type == LibraryType.SHARED) {
            return "libraries/shared/" + filename;
        } else {
            return "libraries/orgs/" + orgId + "/" + filename;
        }
    }

    /**
     * 获取在文件系统中的相对路径。
     *
     * @return 文件系统相对路径
     */
    public String getFileSystemPath() {
        if (type == LibraryType.SHARED) {
            return "shared/" + filename;
        } else {
            return "orgs/" + orgId + "/" + filename;
        }
    }
}
