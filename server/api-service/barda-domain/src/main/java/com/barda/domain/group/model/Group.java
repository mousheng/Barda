package com.barda.domain.group.model;

import java.beans.Transient;
import java.util.Comparator;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.barda.domain.group.util.SystemGroups;
import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 该类表示应用中的组。
 * 它继承了HasIdAndAuditing，实现了Comparable<Group>接口。
 * 它使用了Lombok的@Setter和@ToString注解来生成setter和toString方法。
 * 它使用了Spring Data MongoDB的@Document注解来标记该类为MongoDB的文档。
 */
@Setter
@ToString
@Document
public class Group extends HasIdAndAuditing implements Comparable<Group> {

    /**
     * 用于对组进行排序的比较器。
     * 它首先根据是否为所有用户组、开发组、同步组来排序，
     * 然后根据创建时间来排序。
     */
    private static final Comparator<Group> COMPARATOR = Comparator.comparingLong(group -> {
        if (group.isAllUsersGroup()) {
            return 1;
        }
        if (group.isDevGroup()) {
            return 2;
        }
        if (group.isSyncGroup()) {
            return 3;
        }
        return group.getCreatedAt().toEpochMilli();
    });

    /**
     * 组的名称。
     * 它使用了javax.validation.constraints.NotNull注解来保证非空。
     */
    @NotNull
    private String name;

    /**
     * 组所属的组织ID。
     * 它使用了Lombok的@Getter和javax.validation.constraints.NotNull注解来保证非空。
     */
    @Getter
    @NotNull
    private String organizationId;

    /**
     * 标记该组是否为所有用户组。
     */
    @Getter
    private Boolean allUsersGroup;

    /**
     * 组的类型。
     */
    private String type;

    /**
     * 组的动态规则。
     */
    private String dynamicRule;

    /**
     * 同步组的来源。
     */
    private String source;

    /**
     * 同步组的原始部门ID。
     */
    private String rawDepartmentId;

    /**
     * 标记该同步组是否已被删除。
     */
    private boolean syncDeleted;

    /**
     * 获取组的名称，如果是系统组，则返回翻译后的名称。
     *
     * @param locale 本地化信息
     * @return 组的名称
     */
    public String getName(Locale locale) {
        return isSystemGroup() ? SystemGroups.getName(getType(), locale) : name;
    }

    /**
     * 获取组的类型。
     * 如果是所有用户组，则返回SystemGroups.ALL_USER。
     *
     * @return 组的类型
     */
    public String getType() {
        return isAllUsersGroup() ? SystemGroups.ALL_USER : type;
    }

    /**
     * 判断该组是否为所有用户组。
     *
     * @return true表示是所有用户组，false表示不是
     */
    public boolean isAllUsersGroup() {
        return BooleanUtils.isTrue(allUsersGroup) || SystemGroups.ALL_USER.equals(type);
    }

    /**
     * 判断该组是否为开发组。
     *
     * @return true表示是开发组，false表示不是
     */
    public boolean isDevGroup() {
        return SystemGroups.DEV.equals(type);
    }

    /**
     * 判断该组是否为同步组。
     *
     * @return true表示是同步组，false表示不是
     */
    public boolean isSyncGroup() {
        return StringUtils.isNotBlank(source);
    }

    /**
     * 获取同步组的来源。
     *
     * @return 同步组的来源
     */
    public String getSource() {
        return source;
    }

    /**
     * 获取同步组的原始部门ID。
     *
     * @return 同步组的原始部门ID
     */
    public String getRawDepartmentId() {
        return rawDepartmentId;
    }

    /**
     * 判断该同步组是否已被删除。
     *
     * @return true表示已被删除，false表示未被删除
     */
    public boolean isSyncDeleted() {
        return syncDeleted;
    }

    /**
     * 判断该组是否为系统组。
     *
     * @return true表示是系统组，false表示不是
     */
    @Transient
    @JsonIgnore
    public boolean isSystemGroup() {
        return isAllUsersGroup()
                || isDevGroup();
    }

    /**
     * 判断该组是否不是系统组。
     *
     * @return true表示不是系统组，false表示是
     */
    @Transient
    @JsonIgnore
    public boolean isNotSystemGroup() {
        return !isSystemGroup();
    }

    /**
     * 实现了Comparable<Group>接口的compareTo方法，
     * 用于对组进行排序。
     *
     * @param o 要比较的组
     * @return 排序结果
     */
    @Override
    public int compareTo(@Nonnull Group o) {
        return COMPARATOR.compare(this, o);
    }

    /**
     * 获取组的创建时间。
     *
     * @return 组的创建时间（毫秒）
     */
    public long getCreateTime() {
        return createdAt != null ? createdAt.toEpochMilli() : 0;
    }

    /**
     * 获取组的动态规则。
     *
     * @return 组的动态规则
     */
    public String getDynamicRule() {
        return dynamicRule;
    }

    /**
     * 判断该组是否为动态组。
     *
     * @return true表示是动态组，false表示不是
     */
    @Transient
    @JsonIgnore
    public boolean isDynamic() {
        return StringUtils.isNotBlank(dynamicRule);
    }
}
