package com.barda.api.usermanagement.view;

import javax.validation.constraints.NotNull;

/**
 * 创建群组的请求类。
 */
public class CreateGroupRequest {

    /**
     * 群组名称。
     * 不能为空。
     */
    @NotNull
    private String name;

    /**
     * 动态规则。
     */
    private String dynamicRule;

    /**
     * 获取群组名称。
     *
     * @return 群组名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置群组名称。
     *
     * @param name 群组名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取动态规则。
     *
     * @return 动态规则
     */
    public String getDynamicRule() {
        return dynamicRule;
    }

    /**
     * 设置动态规则。
     *
     * @param dynamicRule 动态规则
     */
    public void setDynamicRule(String dynamicRule) {
        this.dynamicRule = dynamicRule;
    }
}
