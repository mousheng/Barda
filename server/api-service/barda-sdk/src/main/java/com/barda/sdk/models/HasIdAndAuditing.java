package com.barda.sdk.models;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 定义了 ID 和审计功能的抽象类。
 * 所有继承 HasIdAndAuditing 的类都将拥有 ID 和审计字段。
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class HasIdAndAuditing implements Persistable<String>, VersionedModel {

    private static final long serialVersionUID = 7459916000501322717L;

    /**
     * 对象的唯一标识符。
     */
    @Id
    @JsonProperty(index = 1)
    private String id;

    /**
     * 对象创建时间。
     */
    @CreatedDate
    @JsonIgnore
    protected Instant createdAt;

    /**
     * 对象最后一次更新时间。
     * 该字段已添加了 @Indexed 注解，可以对其进行索引以提高查询效率。
     */
    @Indexed
    @JsonIgnore
    @LastModifiedDate
    protected Instant updatedAt;

    /**
     * 创建对象的用户。
     */
    @CreatedBy
    protected String createdBy;

    /**
     * 最后一次修改对象的用户。
     * 该字段已添加了 @JsonIgnore 注解，在 JSON 序列化时将被忽略。
     */
    @JsonIgnore
    @LastModifiedBy
    protected String modifiedBy;

    /**
     * {@inheritDoc}
     *
     * 检查对象的 ID 是否为空，以确定该对象是否为新创建的。
     *
     * @return 如果 ID 为空，返回 true，否则返回 false
     */
    @JsonIgnore
    @Override
    public boolean isNew() {
        return this.getId() == null;
    }


}
