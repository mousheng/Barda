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
 * 库查询的MongoDB文档表示。
 * 该类扩展了{@link HasIdAndAuditing}并用{@link Document}进行了注释，表明它应该存储为MongoDB文档。
 * 它包含用于组织ID、名称和libraryQueryDSL的字段。
 * libraryQueryDSL字段是一个map，用于保存库查询的查询DSL。
 */
@Document
@Getter
@Builder
public class LibraryQuery extends HasIdAndAuditing {

    /**
     * 与此库查询关联的组织的ID。
     */
    private final String organizationId;

    /**
     * 库查询的名称。
     */
    private final String name;

    /**
     * 库查询的查询DSL。
     * 它是一个Map，其中键是字符串，值是Object。
     */
    private final Map<String, Object> libraryQueryDSL;

    /**
     * 库查询的构造函数。
     *
     * @param organizationId 与此库查询关联的组织的ID
     * @param name 库查询的名称
     * @param libraryQueryDSL 库查询的查询DSL
     */
    @JsonCreator
    public LibraryQuery(@JsonProperty("organizationId") String organizationId,
            @JsonProperty("name") String name,
            @JsonProperty("libraryQueryDSL") Map<String, Object> libraryQueryDSL) {
        this.name = name;
        this.organizationId = organizationId;
        this.libraryQueryDSL = libraryQueryDSL;
    }

    /**
     * 库查询的查询对象的懒加载提供者。
     * 它使用Guava的memoize函数来实现懒加载。
     * 它将libraryQueryDSL中的查询部分转换为BaseQuery对象。
     */
    @Transient
    private final Supplier<BaseQuery> baseQuerySupplier = memoize(() ->
            JsonUtils.fromJson(JsonUtils.toJson(getLibraryQueryDSL().get("query")), BaseQuery.class));

    /**
     * 获取库查询的查询对象。
     * 它使用baseQuerySupplier来获取查询对象。
     *
     * @return 库查询的查询对象
     */
    @Transient
    public BaseQuery getQuery() {
        return baseQuerySupplier.get();
    }

}