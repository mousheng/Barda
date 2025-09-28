package com.barda.api.usermanagement.view;

import com.barda.domain.organization.model.Organization;

/**
 * 组织和访问者角色视图类。
 */
public class OrgAndVisitorRoleView {

    /**
     * 组织。
     */
    private final Organization org;

    /**
     * 访问者的角色。
     */
    private final String role;

    /**
     * 构造器。
     *
     * @param org 组织
     * @param role 访问者的角色
     */
    public OrgAndVisitorRoleView(Organization org, String role) {
        this.org = org;
        this.role = role;
    }

    /**
     * 获取组织。
     *
     * @return 组织
     */
    public Organization getOrg() {
        return org;
    }

    /**
     * 获取访问者的角色。
     *
     * @return 访问者的角色
     */
    public String getRole() {
        return role;
    }
}