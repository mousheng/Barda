package com.barda.api.usermanagement.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 更新角色请求类。
 * 该类使用 Lombok 注解来生成 getter、setter、无参构造器和 toString 方法。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UpdateRoleRequest {

    /**
     * 用户 ID。
     */
    String userId;

    /**
     * 角色。
     */
    String role;
}
