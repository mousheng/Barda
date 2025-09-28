package com.barda.domain.user.model;

import static com.google.common.collect.Maps.newHashMap;
import static com.barda.domain.user.constant.UserStatusType.HAS_SHOW_NEW_USER_GUIDANCE;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;

import lombok.Builder;

/**
 * 用户状态类，使用Builder模式创建实例，并使用MongoDB的Document注解。
 */
@Builder
@Document
public class UserStatus {

    /**
     * 用户ID
     */
    @Id
    private final String id;

    /**
     * 是否已展示新用户指南
     */
    private final Boolean hasShowNewUserGuidance;

    /**
     * 用户是否已被禁用
     */
    private final Boolean banned;

    /**
     * 其他用户状态信息
     */
    private final Map<String, Object> statusMap;

    /**
     * 私有构造函数，使用Builder模式创建实例
     */
    @JsonCreator
    public UserStatus(String id, Boolean hasShowNewUserGuidance, Boolean banned, Map<String, Object> statusMap) {
        this.id = id;
        this.hasShowNewUserGuidance = hasShowNewUserGuidance;
        this.banned = banned;
        this.statusMap = statusMap;
    }

    /**
     * 获取用户ID
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * 获取用户状态信息Map，如果Map为空，返回包含是否已展示新用户指南的Map
     * @return Map<String, Object>
     */
    public Map<String, Object> getStatusMap() {
        if (statusMap == null) {
            return ImmutableMap.of(HAS_SHOW_NEW_USER_GUIDANCE.getValue(), isTrue(hasShowNewUserGuidance));
        }

        if (statusMap.containsKey(HAS_SHOW_NEW_USER_GUIDANCE.getValue())) {
            return statusMap;
        }

        Map<String, Object> result = newHashMap(statusMap);
        result.put(HAS_SHOW_NEW_USER_GUIDANCE.getValue(), isTrue(hasShowNewUserGuidance));
        return result;
    }

    /**
     * 判断是否已展示新用户指南
     * @return boolean
     */
    public boolean hasShowNewUserGuidance() {
        return isTrue(hasShowNewUserGuidance);
    }

    /**
     * 判断用户是否已被禁用
     * @return Boolean
     */
    public Boolean isBanned() {
        return isTrue(banned);
    }

    /**
     * 私有方法，判断是否为true
     * @param value Boolean值
     * @return boolean
     */
    private boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
