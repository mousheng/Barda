package com.barda.api.application.view;

import java.time.Instant;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.barda.api.home.FolderInfoView;
import com.barda.domain.application.model.ApplicationStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * 应用信息视图类，用于在应用列表中返回应用的基础信息。
 */
@Builder
@Getter
public class ApplicationInfoView {
    /**
     * 所属组织ID
     */
    private final String orgId;
    /**
     * 应用ID
     */
    private final String applicationId;
    /**
     * 应用名称
     */
    private final String name;
    /**
     * 创建时间
     */
    private final long createAt;
    /**
     * 创建者
     */
    private final String createBy;
    /**
     * 用户在当前应用中的最大角色
     */
    private final String role;
    /**
     * 应用类型
     * @see com.barda.domain.application.model.ApplicationType
     */
    private final int applicationType;
    /**
     * 应用状态
     */
    private final ApplicationStatus applicationStatus;
    /**
     * 模块大小，仅在返回模块信息时有效
     */
    private final Object containerSize;
    /**
     * 所属文件夹ID，若为null则表示应用不在文件夹中
     */
    @Nullable
    private final String folderId;

    /**
     * 用户最后访问该应用的时间，若为null则表示用户从未访问过
     */
    @Nullable
    private final Instant lastViewTime;
    /**
     * 应用的最后更新时间
     */
    private final Instant lastModifyTime;

    /**
     * 应用是否对所有人公开
     */
    private final boolean publicToAll;

    /**
     * 获取用户最后访问该应用的时间，若为null则返回0
     * @return 最后访问时间的毫秒数
     */
    public long getLastViewTime() {
        return lastViewTime == null ? 0 : lastViewTime.toEpochMilli();
    }

    /**
     * 获取应用的最后更新时间，若为null则返回0
     * @return 最后更新时间的毫秒数
     */
    public long getLastModifyTime() {
        return lastModifyTime == null ? 0 : lastModifyTime.toEpochMilli();
    }

    /**
     * 用于前端判断是否为文件夹
     * @see FolderInfoView#isFolder()
     * @return 始终返回false，表示当前对象不是文件夹
     */
    public boolean isFolder() {
        return false;
    }
}
