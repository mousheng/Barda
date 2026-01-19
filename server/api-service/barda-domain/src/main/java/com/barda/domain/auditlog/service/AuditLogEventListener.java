package com.barda.domain.auditlog.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.barda.infra.event.AbstractEvent;
import com.barda.infra.event.ApplicationCommonEvent;
import com.barda.infra.event.FolderCommonEvent;
import com.barda.infra.event.LibraryQueryEvent;
import com.barda.infra.event.QueryExecutionEvent;
import com.barda.infra.event.datasource.DatasourceEvent;
import com.barda.infra.event.datasource.DatasourcePermissionEvent;
import com.barda.infra.event.group.BaseGroupEvent;
import com.barda.infra.event.groupmember.BaseGroupMemberEvent;
import com.barda.infra.event.user.UserLoginEvent;
import com.barda.infra.event.user.UserLogoutEvent;

/**
 * 审计日志事件监听器
 * 监听各种业务事件并记录审计日志
 */
@Component
public class AuditLogEventListener {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 监听用户登录事件
     */
    @EventListener
    public void onUserLoginEvent(UserLoginEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("loginWay", event.getSource());
        this.record(event, detail);
    }

    /**
     * 监听用户登出事件
     */
    @EventListener
    public void onUserLogoutEvent(UserLogoutEvent event) {
        this.record(event, null);
    }

    /**
     * 监听应用操作事件
     */
    @EventListener
    public void onApplicationCommonEvent(ApplicationCommonEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("applicationId", event.getApplicationId());
        detail.put("applicationName", event.getApplicationName());
        if (StringUtils.isNoneBlank(event.getFolderId(), event.getFolderName())) {
            detail.put("folderId", event.getFolderId());
            detail.put("folderName", event.getFolderName());
        }
        this.record(event, detail);
    }

    /**
     * 监听文件夹操作事件
     */
    @EventListener
    public void onFolderCommonEvent(FolderCommonEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("folderId", event.getId());
        detail.put("folderName", event.getName());
        this.record(event, detail);
    }

    /**
     * 监听查询执行事件
     */
    @EventListener
    public void onQueryExecutionEvent(QueryExecutionEvent event) {
        // 直接使用事件中的detail
        this.record(event, event.getDetail());
    }

    /**
     * 监听分组事件
     */
    @EventListener
    public void onGroupEvent(BaseGroupEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("groupId", event.getGroupId());
        detail.put("groupName", event.getGroupName());
        this.record(event, detail);
    }

    /**
     * 监听分组成员事件
     */
    @EventListener
    public void onGroupMemberEvent(BaseGroupMemberEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("groupId", event.getGroupId());
        detail.put("groupName", event.getGroupName());
        detail.put("memberId", event.getMemberId());
        detail.put("memberName", event.getMemberName());
        detail.put("memberRole", event.getMemberRole());
        this.record(event, detail);
    }

    /**
     * 监听数据源事件
     */
    @EventListener
    public void onDatasourceEvent(DatasourceEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("datasourceId", event.getDatasourceId());
        detail.put("datasourceName", event.getName());
        detail.put("datasourceType", event.getType());
        this.record(event, detail);
    }

    /**
     * 监听数据源权限事件
     */
    @EventListener
    public void onDatasourcePermissionEvent(DatasourcePermissionEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("datasourceId", event.getDatasourceId());
        detail.put("datasourceName", event.getName());
        detail.put("datasourceType", event.getType());
        detail.put("userIds", event.getUserIds());
        detail.put("groupIds", event.getGroupIds());
        detail.put("role", event.getRole());
        this.record(event, detail);
    }

    /**
     * 监听库查询事件
     */
    @EventListener
    public void onLibraryQueryEvent(LibraryQueryEvent event) {
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", event.getId());
        detail.put("name", event.getName());
        this.record(event, detail);
    }

    /**
     * 统一记录方法
     */
    private void record(AbstractEvent event, Map<String, Object> detail) {
        this.auditLogService.record(
                event.getOrgId(),
                event.getUserId(),
                event.getEventType(),
                detail
        );
    }
}
