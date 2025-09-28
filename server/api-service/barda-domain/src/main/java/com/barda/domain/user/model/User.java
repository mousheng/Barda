package com.barda.domain.user.model;

import static com.google.common.base.Suppliers.memoize;
import static com.barda.infra.util.AssetUtils.toAssetPath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * 用户类，继承自HasIdAndAuditing类，并使用MongoDB的Document注解。
 */
@Getter
@Setter
@ToString
@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends HasIdAndAuditing {

    private static final OrgTransformedUserInfo EMPTY_TRANSFORMED_USER_INFO = new OrgTransformedUserInfo();

    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 第三方头像链接
     */
    private String tpAvatarLink;

    /**
     * 用户状态
     */
    private UserState state;

    /**
     * 用户是否启用
     */
    private Boolean isEnabled = true;

    /**
     * 仅在表单登录中使用，用于反序列化
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 标识用户是否为匿名用户
     */
    @Transient
    Boolean isAnonymous = false;

    /**
     * 用户的连接集合
     */
    private Set<Connection> connections;

    /**
     * 获取头像URL，如果头像为空，返回第三方头像链接
     */
    @Transient
    @JsonIgnore
    private Supplier<String> avatarUrl = memoize(() -> StringUtils.isNotBlank(avatar) ? toAssetPath(avatar) : tpAvatarLink);

    /**
     * 标识用户是否为新用户
     */
    @Transient
    @JsonIgnore
    private Boolean isNewUser = false;

    /**
     * 标识用户是否已设置昵称
     */
    private boolean hasSetNickname;

    /**
     * 组织信息的转换结果
     */
    private OrgTransformedUserInfo orgTransformedUserInfo;

    /**
     * 判断用户是否为匿名用户
     * @return boolean
     */
    @Transient
    @JsonIgnore
    public boolean isAnonymous() {
        return Boolean.TRUE.equals(isAnonymous);
    }

    /**
     * 获取用户的连接集合，如果为空，返回空集合
     * @return Set<Connection>
     */
    public Set<Connection> getConnections() {
        if (this.connections == null) {
            this.connections = new HashSet<>();
        }
        return this.connections;
    }

    /**
     * 获取头像URL
     * @return String
     */
    @JsonIgnore
    public String getAvatarUrl() {
        return avatarUrl.get();
    }

    /**
     * 获取组织信息的转换结果
     * @return OrgTransformedUserInfo
     */
    public OrgTransformedUserInfo getOrgTransformedUserInfo() {
        return orgTransformedUserInfo;
    }

    /**
     * 组织信息的转换结果类
     */
    public static class OrgTransformedUserInfo extends HashMap<String, TransformedUserInfo> {

        /**
         * 获取指定组织的转换结果
         * @param orgId 组织ID
         * @return TransformedUserInfo
         */
        public TransformedUserInfo get(String orgId) {
            return super.get(orgId);
        }

        /**
         * 设置指定组织的转换结果
         * @param orgId 组织ID
         * @param transformedUserInfo 转换结果
         */
        public void set(String orgId, TransformedUserInfo transformedUserInfo) {
            super.put(orgId, transformedUserInfo);
        }
    }

    /**
     * 转换后的组织信息类
     */
    public record TransformedUserInfo(long updateTime, Map<String, Object> extra) {

    }

    /**
     * 标记用户为已删除状态
     */
    public void markAsDeleted() {
        this.setState(UserState.DELETED);
        this.setIsEnabled(false);
        SetUtils.emptyIfNull(this.getConnections())
                .forEach(connection -> connection.setSource(
                        connection.getSource() + "(User deleted at " + System.currentTimeMillis() / 1000 + ")"));
    }
}
