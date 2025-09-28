package com.barda.api.home;

import java.util.List;

import com.barda.api.application.view.ApplicationInfoView;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.user.model.User;

/**
 * 用户主页视图类，用于表示用户主页的相关信息。
 */
public class UserHomepageView {

    /**
     * 用户信息。
     */
    private User user;

    /**
     * 所属的组织机构信息。
     */
    private Organization organization;

    /**
     * 应用信息列表。
     */
    private List<ApplicationInfoView> applicationInfoViews;

    /**
     * 文件夹信息列表。
     */
    private List<FolderInfoView> folderInfoViews;

    /**
     * 获取用户信息。
     *
     * @return 用户信息
     */
    public User getUser() {
        return user;
    }

    /**
     * 设置用户信息。
     *
     * @param user 用户信息
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 获取所属的组织机构信息。
     *
     * @return 组织机构信息
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * 设置所属的组织机构信息。
     *
     * @param organization 组织机构信息
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * 获取应用信息列表。
     *
     * @return 应用信息列表
     */
    public List<ApplicationInfoView> getHomeApplicationViews() {
        return applicationInfoViews;
    }

    /**
     * 设置应用信息列表。
     *
     * @param applicationInfoViews 应用信息列表
     */
    public void setHomeApplicationViews(List<ApplicationInfoView> applicationInfoViews) {
        this.applicationInfoViews = applicationInfoViews;
    }

    /**
     * 获取文件夹信息列表。
     *
     * @return 文件夹信息列表
     */
    public List<FolderInfoView> getFolderInfoViews() {
        return folderInfoViews;
    }

    /**
     * 设置文件夹信息列表。
     *
     * @param folderInfoViews 文件夹信息列表
     */
    public void setFolderInfoViews(List<FolderInfoView> folderInfoViews) {
        this.folderInfoViews = folderInfoViews;
    }
}
