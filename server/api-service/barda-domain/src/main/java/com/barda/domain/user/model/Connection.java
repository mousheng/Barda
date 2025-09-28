package com.barda.domain.user.model;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.barda.sdk.constants.AuthSourceConstants;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 该类表示用户与第三方认证源之间的连接。
 */
@Getter
@Setter
@Builder
public class Connection {

    private static final long serialVersionUID = -9218373922209100577L;

    /**
     * 认证ID
     */
    private String authId;

    /**
     * 认证源
     */
    @NotEmpty
    private String source;

    /**
     * 原始ID，只在反序列化时使用
     */
    @NotEmpty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final String rawId;

    /**
     * 名称
     */
    private final String name;

    /**
     * 头像
     */
    private final String avatar;

    /**
     * 组织ID集合
     */
    private Set<String> orgIds;

    /**
     * 认证连接的令牌，只在反序列化时使用
     */
    @Nullable
    @JsonProperty(access = Access.WRITE_ONLY)
    private ConnectionAuthToken authConnectionAuthToken;

    /**
     * 原始用户信息
     */
    private Map<String, Object> rawUserInfo;

    /**
     * 令牌集合
     */
    private Set<String> tokens;

    /**
     * 私有构造器，使用builder模式创建实例
     */
    @JsonCreator
    private Connection(String authId, String source, String rawId, String name, String avatar, Set<String> orgIds, @Nullable
    ConnectionAuthToken authConnectionAuthToken, Map<String, Object> rawUserInfo, Set<String> tokens) {
        this.authId = authId;
        this.source = source;
        this.rawId = rawId;
        this.name = name;
        this.avatar = avatar;
        this.orgIds = CollectionUtils.isEmpty(orgIds) ? new HashSet<>() : orgIds;
        this.authConnectionAuthToken = authConnectionAuthToken;
        this.rawUserInfo = rawUserInfo;
        this.tokens = tokens;
    }

    /**
     * 获取Connection的Builder
     * @return Connection.ConnectionBuilder
     */
    public static Connection.ConnectionBuilder builder() {
        return new ConnectionBuilder();
    }

    /**
     * 获取令牌集合，如果为空，返回空集合
     * @return Set<String>
     */
    public Set<String> getTokens() {
        return SetUtils.emptyIfNull(this.tokens);
    }

    /**
     * 向令牌集合中添加令牌
     * @param token 要添加的令牌
     */
    public void addToken(String token) {
        if (this.tokens == null) {
            this.tokens = new HashSet<>();
        }
        this.tokens.add(token);
    }

    /**
     * 从令牌集合中移除令牌
     * @param token 要移除的令牌
     */
    public void removeToken(String token) {
        if (this.tokens == null) {
            this.tokens = new HashSet<>();
        }
        this.tokens.remove(token);
    }

    /**
     * 获取认证源
     * @return String
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置认证源
     * @param source 认证源
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取组织ID集合，返回不可修改的集合
     * @return Set<String>
     */
    @JsonIgnore
    public Set<String> getOrgIds() {
        return orgIds;
    }

    /**
     * 向组织ID集合中添加组织ID
     * @param orgId 要添加的组织ID
     */
    public void addOrg(String orgId) {
        orgIds.add(orgId);
    }

    /**
     * 判断组织ID集合中是否包含指定组织ID
     * @param orgId 要判断的组织ID
     * @return boolean
     */
    public boolean containOrg(String orgId) {
        return orgIds.contains(orgId);
    }

    /**
     * 判断是否匹配指定的第三方认证源和组织ID
     * @param sourceType 要匹配的认证源
     * @param orgId 要匹配的组织ID
     * @return boolean
     */
    public boolean matchThirdPartyLoginSourceInCloud(String sourceType, String orgId) {
        return StringUtils.equals(sourceType, source) && containOrg(orgId);
    }

    /**
     * 获取原始用户信息，如果认证源是EMAIL，返回包含email的Map
     * @return Map<String, Object>
     */
    public Map<String, Object> getRawUserInfo() {
        if (AuthSourceConstants.EMAIL.equals(this.getSource())) {
            return Map.of("email", this.getRawId());
        }
        return emptyIfNull(rawUserInfo);
    }

    /**
     * 判断是否匹配指定的第三方认证源
     * @param sourceType 要匹配的认证源
     * @return boolean
     */
    public boolean matchThirdPartyLoginSourceInSelfHost(String sourceType) {
        return StringUtils.equals(sourceType, source);
    }
}
