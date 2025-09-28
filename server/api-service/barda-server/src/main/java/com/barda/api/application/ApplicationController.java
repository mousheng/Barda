package com.barda.api.application;

import static com.barda.infra.event.EventType.APPLICATION_CREATE;
import static com.barda.infra.event.EventType.APPLICATION_DELETE;
import static com.barda.infra.event.EventType.APPLICATION_RECYCLED;
import static com.barda.infra.event.EventType.APPLICATION_RESTORE;
import static com.barda.infra.event.EventType.APPLICATION_UPDATE;
import static com.barda.infra.event.EventType.VIEW;
import static com.barda.sdk.exception.BizError.INVALID_PARAMETER;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.api.application.view.ApplicationInfoView;
import com.barda.api.application.view.ApplicationPermissionView;
import com.barda.api.application.view.ApplicationView;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.home.UserHomeApiService;
import com.barda.api.home.UserHomepageView;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.application.model.Application;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.model.ApplicationType;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 应用控制器类，提供应用相关的API。
 *
 */
@Slf4j
@RestController
@RequestMapping(value = {Url.APPLICATION_URL, NewUrl.APPLICATION_URL})
public class ApplicationController {

    /**
     * 用户主页API服务。
     */
    @Autowired
    private UserHomeApiService userHomeApiService;

    /**
     * 应用API服务。
     */
    @Autowired
    private ApplicationApiService applicationApiService;

    /**
     * 事件发布器。
     */
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 创建应用。
     *
     * @param createApplicationRequest 创建应用的请求。
     * @return 一个Mono，发出包含应用视图的ResponseView。
     */
    @PostMapping
    public Mono<ResponseView<ApplicationView>> create(@RequestBody CreateApplicationRequest createApplicationRequest) {
        return applicationApiService.create(createApplicationRequest)
                .delayUntil(applicationView -> businessEventPublisher.publishApplicationCommonEvent(applicationView, APPLICATION_CREATE))
                .map(ResponseView::success);
    }

    /**
     * 根据模板创建应用程序，并发布应用程序创建事件。
     *
     * @param templateId 模板ID
     * @return 包含应用程序视图对象的Mono，如果成功创建则返回成功的响应视图
     */
    @PostMapping("/createFromTemplate")
    public Mono<ResponseView<ApplicationView>> createFromTemplate(@RequestParam String templateId) {
        return applicationApiService.createFromTemplate(templateId)
                // 延迟执行事件发布，然后继续流水线
                .delayUntil(applicationView -> businessEventPublisher.publishApplicationCommonEvent(applicationView, APPLICATION_CREATE))
                // 将结果映射为成功的响应视图
                .map(ResponseView::success);
    }

    /**
     * 根据应用程序ID回收应用程序，并发布应用程序回收事件。
     *
     * @param applicationId 应用程序ID
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @PutMapping("/recycle/{applicationId}")
    public Mono<ResponseView<Boolean>> recycle(@PathVariable String applicationId) {
        return applicationApiService.recycle(applicationId)
                // 延迟执行事件发布，然后继续流水线
                .delayUntil(__ -> businessEventPublisher.publishApplicationCommonEvent(applicationId, null, APPLICATION_RECYCLED))
                // 将结果映射为成功的响应视图
                .map(ResponseView::success);
    }

    /**
     * 根据应用程序ID恢复应用程序，并发布应用程序恢复事件。
     *
     * @param applicationId 应用程序ID
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @PutMapping("/restore/{applicationId}")
    public Mono<ResponseView<Boolean>> restore(@PathVariable String applicationId) {
        return applicationApiService.restore(applicationId)
                // 延迟执行事件发布，然后继续流水线
                .delayUntil(__ -> businessEventPublisher.publishApplicationCommonEvent(applicationId, null, APPLICATION_RESTORE))
                // 将结果映射为成功的响应视图
                .map(ResponseView::success);
    }

    /**
     * 获取回收站中的应用程序列表。
     *
     * @return 包含应用程序信息视图列表的Mono，表示操作是否成功的响应视图
     */
    @GetMapping("/recycle/list")
    public Mono<ResponseView<List<ApplicationInfoView>>> getRecycledApplications() {
        return applicationApiService.getRecycledApplications()
                .collectList()
                .map(ResponseView::success);
    }

    /**
     * 删除指定应用程序，并发布应用程序删除事件。
     *
     * @param applicationId 应用程序ID
     * @return 包含应用程序视图对象的Mono，表示操作是否成功的响应视图
     */
    @DeleteMapping("/{applicationId}")
    public Mono<ResponseView<ApplicationView>> delete(@PathVariable String applicationId) {
        return applicationApiService.delete(applicationId)
                // 延迟执行事件发布，然后继续流水线
                .delayUntil(applicationView -> businessEventPublisher.publishApplicationCommonEvent(applicationView, APPLICATION_DELETE))
                // 将结果映射为成功的响应视图
                .map(ResponseView::success);
    }

    /**
     * 获取编辑中的应用程序。
     *
     * @param applicationId 应用程序ID
     * @return 包含应用程序视图对象的Mono，表示操作是否成功的响应视图
     */
    @GetMapping("/{applicationId}")
    public Mono<ResponseView<ApplicationView>> getEditingApplication(@PathVariable String applicationId) {
        return applicationApiService.getEditingApplication(applicationId)
                // 更新用户最后查看应用程序时间
                .delayUntil(__ -> applicationApiService.updateUserApplicationLastViewTime(applicationId))
                .map(ResponseView::success);
    }

    /**
     * 获取已发布的应用程序，并发布应用程序查看事件。
     *
     * @param applicationId 应用程序ID
     * @return 包含应用程序视图对象的Mono，表示操作是否成功的响应视图
     */
    @GetMapping("/{applicationId}/view")
    public Mono<ResponseView<ApplicationView>> getPublishedApplication(@PathVariable String applicationId) {
        return applicationApiService.getPublishedApplication(applicationId)
                // 更新用户最后查看应用程序时间
                .delayUntil(applicationView -> applicationApiService.updateUserApplicationLastViewTime(applicationId))
                // 发布应用程序查看事件
                .delayUntil(applicationView -> businessEventPublisher.publishApplicationCommonEvent(applicationView, VIEW))
                // 将结果映射为成功的响应视图
                .map(ResponseView::success);
    }

    /**
     * 更新应用程序信息，并发布应用程序更新事件。
     *
     * @param applicationId 应用程序ID
     * @param newApplication 新的应用程序信息
     * @return 包含应用程序视图对象的Mono，表示操作是否成功的响应视图
     */
    @PutMapping("/{applicationId}")
    public Mono<ResponseView<ApplicationView>> update(@PathVariable String applicationId,
            @RequestBody Application newApplication) {
        return applicationApiService.update(applicationId, newApplication)
                // 延迟执行事件发布，然后继续流水线
                .delayUntil(applicationView -> businessEventPublisher.publishApplicationCommonEvent(applicationView, APPLICATION_UPDATE))
                // 将结果映射为成功的响应视图
                .map(ResponseView::success);
    }

    /**
     * 发布应用程序。
     *
     * @param applicationId 应用程序ID
     * @return 包含应用程序视图对象的Mono，表示操作是否成功的响应视图
     */
    @PostMapping("/{applicationId}/publish")
    public Mono<ResponseView<ApplicationView>> publish(@PathVariable String applicationId) {
        return applicationApiService.publish(applicationId)
                // 将结果映射为成功的响应视图
                .map(ResponseView::success);
    }

    /**
     * 获取用户首页。
     *
     * @param applicationType 应用程序类型（可选）
     * @return 包含用户首页视图对象的Mono，表示操作是否成功的响应视图
     */
    @GetMapping("/home")
    public Mono<ResponseView<UserHomepageView>> getUserHomePage(@RequestParam(required = false, defaultValue = "0") int applicationType) {
        ApplicationType type = ApplicationType.fromValue(applicationType);
        return userHomeApiService.getUserHomePageView(type)
                .map(ResponseView::success);
    }

    /**
     * 获取应用程序列表。
     *
     * @param applicationType 应用程序类型（可选）
     * @param applicationStatus 应用程序状态（可选）
     * @param withContainerSize 是否返回容器大小信息（默认为true）
     * @return 包含应用程序信息视图列表的Mono，表示操作是否成功的响应视图
     */
    @GetMapping("/list")
    public Mono<ResponseView<List<ApplicationInfoView>>> getApplications(@RequestParam(required = false) Integer applicationType,
            @RequestParam(required = false) ApplicationStatus applicationStatus,
            @RequestParam(defaultValue = "true") boolean withContainerSize) {
        ApplicationType applicationTypeEnum = applicationType == null ? null : ApplicationType.fromValue(applicationType);
        return userHomeApiService.getAllAuthorisedApplications4CurrentOrgMember(applicationTypeEnum, applicationStatus, withContainerSize)
                .collectList()
                .map(ResponseView::success);
    }

    /**
     * 更新应用程序的权限。
     *
     * @param applicationId         应用程序ID
     * @param permissionId          权限ID
     * @param updatePermissionRequest 更新权限请求对象
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @PutMapping("/{applicationId}/permissions/{permissionId}")
    public Mono<ResponseView<Boolean>> updatePermission(@PathVariable String applicationId,
            @PathVariable String permissionId,
            @RequestBody UpdatePermissionRequest updatePermissionRequest) {
        ResourceRole role = ResourceRole.fromValue(updatePermissionRequest.role());
        if (role == null) {
            return ofError(INVALID_PARAMETER, "INVALID_PARAMETER", updatePermissionRequest);
        }

        return applicationApiService.updatePermission(applicationId, permissionId, role)
                .map(ResponseView::success);
    }

    /**
     * 移除应用程序的权限。
     *
     * @param applicationId 应用程序ID
     * @param permissionId  权限ID
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @DeleteMapping("/{applicationId}/permissions/{permissionId}")
    public Mono<ResponseView<Boolean>> removePermission(
            @PathVariable String applicationId,
            @PathVariable String permissionId) {

        return applicationApiService.removePermission(applicationId, permissionId)
                .map(ResponseView::success);
    }

    /**
     * 授予应用程序权限。
     *
     * @param applicationId 应用程序ID
     * @param request       批量添加权限请求对象
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @PutMapping("/{applicationId}/permissions")
    public Mono<ResponseView<Boolean>> grantPermission(
            @PathVariable String applicationId,
            @RequestBody BatchAddPermissionRequest request) {
        ResourceRole role = ResourceRole.fromValue(request.role());
        if (role == null) {
            return ofError(INVALID_PARAMETER, "INVALID_PARAMETER", request.role());
        }
        return applicationApiService.grantPermission(applicationId,
                        emptyIfNull(request.userIds()),
                        emptyIfNull(request.groupIds()),
                        role)
                .map(ResponseView::success);
    }

    /**
     * 获取应用程序的权限。
     *
     * @param applicationId 应用程序ID
     * @return 包含应用程序权限视图对象的Mono，表示操作是否成功的响应视图
     */
    @GetMapping("/{applicationId}/permissions")
    public Mono<ResponseView<ApplicationPermissionView>> getApplicationPermissions(@PathVariable String applicationId) {
        return applicationApiService.getApplicationPermissions(applicationId)
                .map(ResponseView::success);
    }

    /**
     * 设置应用程序对所有人公开或取消对所有人的公开。
     *
     * @param applicationId 应用程序ID
     * @param request       应用程序公开设置请求对象
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @PutMapping("/{applicationId}/public-to-all")
    public Mono<ResponseView<Boolean>> setApplicationPublicToAll(@PathVariable String applicationId,
            @RequestBody ApplicationPublicToAllRequest request) {
        return applicationApiService.setApplicationPublicToAll(applicationId, request.publicToAll())
                .map(ResponseView::success);
    }


    /**
     * 私有记录类，用于批量添加权限的请求。
     *
     * @param role   角色。
     * @param userIds 用户ID集合。
     * @param groupIds 组ID集合。
     */
    private record BatchAddPermissionRequest(String role, Set<String> userIds, Set<String> groupIds) {
    }

    /**
     * 私有记录类，用于设置应用公开状态的请求。
     *
     * @param publicToAll 应用是否对所有用户公开。
     */
    private record ApplicationPublicToAllRequest(Boolean publicToAll) {
        @Override
        public Boolean publicToAll() {
            return BooleanUtils.isTrue(publicToAll);
        }
    }

    private record UpdatePermissionRequest(String role) {
    }

    /**
     * 私有记录类，用于创建应用的请求。
     *
     * @param organizationId 组织ID。
     * @param name 应用名称。
     * @param applicationType 应用类型。
     * @param publishedApplicationDSL 已发布应用的DSL。
     * @param editingApplicationDSL 编辑中的应用的DSL。
     * @param folderId 文件夹ID。
     */
    public record CreateApplicationRequest(@JsonProperty("orgId") String organizationId,
                                           String name,
                                           Integer applicationType,
                                           Map<String, Object> publishedApplicationDSL,
                                           Map<String, Object> editingApplicationDSL,
                                           @Nullable String folderId) {
    }
}
