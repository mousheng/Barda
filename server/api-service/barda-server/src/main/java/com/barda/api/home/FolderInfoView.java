package com.barda.api.home;

import java.time.Instant;
import java.util.List;

import com.barda.api.application.view.ApplicationInfoView;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用于表示文件夹信息的视图类。
 *
 */
@Getter
@Setter
@Builder
@ToString
public class FolderInfoView {

    /**
     * 组织ID。
     */
    private final String orgId;

    /**
     * 文件夹ID。
     */
    private final String folderId;

    /**
     * 父文件夹ID。
     */
    private final String parentFolderId;

    /**
     * 文件夹名称。
     */
    private final String name;

    /**
     * 创建时间（Unix时间戳）。
     */
    private final Long createAt;

    /**
     * 创建者。
     */
    private final String createBy;

    /**
     * 是否可见。
     */
    private boolean isVisible;

    /**
     * 是否可管理。
     */
    private boolean isManageable;

    /**
     * 子文件夹列表。
     */
    private List<FolderInfoView> subFolders;

    /**
     * 子应用列表。
     */
    private List<ApplicationInfoView> subApplications;

    /**
     * 创建时间。
     */
    private final Instant createTime;

    /**
     * 最后查看时间。
     */
    private final Instant lastViewTime;

    /**
     * 获取创建时间的毫秒数。
     *
     * @return 创建时间的毫秒数。
     */
    public long getCreateTime() {
        return createTime == null ? 0 : createTime.toEpochMilli();
    }

    /**
     * 获取最后查看时间的毫秒数。
     *
     * @return 最后查看时间的毫秒数。
     */
    public long getLastViewTime() {
        return lastViewTime == null ? 0 : lastViewTime.toEpochMilli();
    }

    /**
     * 用于前端判断是否为文件夹。
     *
     * @return true表示是文件夹，false表示不是文件夹。
     *
     * @see ApplicationInfoView#isFolder()
     */
    public boolean isFolder() {
        return true;
    }
}
