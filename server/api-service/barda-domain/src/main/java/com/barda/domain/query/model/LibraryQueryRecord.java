package com.barda.domain.query.model;

import static com.google.common.base.Suppliers.memoize;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.sdk.models.HasIdAndAuditing;
import com.barda.sdk.util.JsonUtils;

import lombok.Builder;
import lombok.Getter;

/**
 * 库查询记录的MongoDB文档表示。
 * 该类扩展了{@link HasIdAndAuditing}并用{@link Document}进行了注释，表明它应该存储为MongoDB文档。
 * 它包含用于库查询ID、标记、提交消息和libraryQueryDSL的字段。
 */
@Document
@Getter
@Builder
public class LibraryQueryRecord extends HasIdAndAuditing {

    /**
     * 与此库查询记录关联的库查询的ID。
     */
    private final String libraryQueryId;

    /**
     * 库查询记录的标记。
     */
    private final String tag;

    /**
     * 库查询记录的提交消息。
     */
    private final String commitMessage;

    /**
     * 库查询记录的查询DSL。
     * 它是一个Map，其中键是字符串，值是Object。
     */
    private final Map<String, Object> libraryQueryDSL;

    /**
     * 库查询记录的构造函数。
     *
     * @param libraryQueryId 与此库查询记录关联的库查询的ID
     * @param tag 库查询记录的标记
     * @param commitMessage 库查询记录的提交消息
     * @param libraryQueryDSL 库查询记录的查询DSL
     */
    @JsonCreator
    public LibraryQueryRecord(@JsonProperty("libraryQueryId") String libraryQueryId,
            @JsonProperty("tag") String tag,
            @JsonProperty("commitMessage") String commitMessage,
            @JsonProperty("libraryQueryDSL") Map<String, Object> libraryQueryDSL) {
        this.libraryQueryId = libraryQueryId;
        this.tag = tag;
        this.commitMessage = commitMessage;
        this.libraryQueryDSL = libraryQueryDSL;
    }

    /**
     * 库查询记录的查询对象的懒加载提供者。
     * 它使用Guava的memoize函数来实现懒加载。
     * 它将libraryQueryDSL中的查询部分转换为BaseQuery对象。
     */
    @Transient
    private final Supplier<BaseQuery> baseQuerySupplier = memoize(() ->
            JsonUtils.fromJson(JsonUtils.toJson(getLibraryQueryDSL().get("query")), BaseQuery.class));

    /**
     * 获取库查询记录的查询对象。
     * 它使用baseQuerySupplier来获取查询对象。
     *
     * @return 库查询记录的查询对象
     */
    @Transient
    public BaseQuery getQuery() {
        return baseQuerySupplier.get();
    }

    /**
     * 获取库查询记录的创建时间戳（毫秒）。
     * 它将createdAt字段转换为毫秒。
     *
     * @return 库查询记录的创建时间戳（毫秒）
     */
    public long getCreateTime() {
        return createdAt.toEpochMilli();
    }
}
