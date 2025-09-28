package com.barda.api.usermanagement.view;

import javax.annotation.Nonnull;

import com.barda.domain.organization.model.Organization;

/**
 * 组织视图类。
 */
public class OrgView {

    /**
     * 组织。
     */
    private final Organization organization;

    /**
     * 构造器。
     *
     * @param organization 组织
     */
    public OrgView(@Nonnull Organization organization) {
        this.organization = organization;
    }

    /**
     * 获取组织 ID。
     *
     * @return 组织 ID
     */
    public String getOrgId() {
        return organization.getId();
    }

    /**
     * 获取组织名称。
     *
     * @return 组织名称
     */
    public String getOrgName() {
        return organization.getName();
    }
}
