package com.barda.api.usermanagement.view;

/**
 * 更新群组请求类。
 */
public class UpdateGroupRequest {

    /**
     * 群组名称。
     */
    private String groupName;

    /**
     * 动态规则。
     */
    private String dynamicRule;

    /**
     * 获取群组名称。
     *
     * @return 群组名称
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * 设置群组名称。
     *
     * @param groupName 群组名称
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
