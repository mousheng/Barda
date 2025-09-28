package com.barda.api.usermanagement.view;

/**
 * 更新用户请求类。
 */
public class UpdateUserRequest {

    /**
     * 用户名称。
     */
    private String name;

    /**
     * 获取用户名称。
     *
     * @return 用户名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置用户名称。
     *
     * @param name 用户名称
     */
    public void setName(String name) {
        this.name = name;
    }
}
