package com.barda.api.usermanagement.view;

/**
 * 更新组织请求类。
 */
public class UpdateOrgRequest {

    /**
     * 组织名称。
     */
    private String orgName;

    /**
     * 联系人名称。
     */
    private String contactName;

    /**
     * 联系人邮箱。
     */
    private String contactEmail;

    /**
     * 联系人电话号码。
     */
    private String contactPhoneNumber;

    /**
     * 获取组织名称。
     *
     * @return 组织名称
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * 设置组织名称。
     *
     * @param orgName 组织名称
     */
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    /**
     * 获取联系人名称。
     *
     * @return 联系人名称
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * 设置联系人名称。
     *
     * @param contactName 联系人名称
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * 获取联系人邮箱。
     *
     * @return 联系人邮箱
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * 设置联系人邮箱。
     *
     * @param contactEmail 联系人邮箱
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * 获取联系人电话号码。
     *
     * @return 联系人电话号码
     */
    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    /**
     * 设置联系人电话号码。
     *
     * @param contactPhoneNumber 联系人电话号码
     */
    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }
}
