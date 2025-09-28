package com.barda.api.usermanagement.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 用于添加成员的请求类。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AddMemberRequest {

    /**
     * 用户 ID。
     */
    String userId;

    /**
     * 角色。
     */
    String role;
}
