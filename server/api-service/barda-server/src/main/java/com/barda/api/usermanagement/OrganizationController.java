package com.barda.api.usermanagement;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.barda.api.authentication.dto.OrganizationDomainCheckResult;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.usermanagement.view.OrgMemberListView;
import com.barda.api.usermanagement.view.OrgView;
import com.barda.api.usermanagement.view.UpdateOrgRequest;
import com.barda.api.usermanagement.view.UpdateRoleRequest;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.model.Organization.OrganizationCommonSettings;
import com.barda.domain.plugin.DatasourceMetaInfo;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;

import reactor.core.publisher.Mono;

/**
 * 组织控制器类。
 * 该类使用 Spring 注解来声明为 REST 控制器并将其映射到特定的 URL 上。
 */
@RestController
@RequestMapping(value = {Url.ORGANIZATION_URL, NewUrl.ORGANIZATION_URL})
public class OrganizationController {

    @Autowired
    private OrgApiService orgApiService;
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 创建组织。
     *
     * @param organization 要创建的组织
     * @return 创建的组织的视图
     */
    @PostMapping
    public Mono<ResponseView<OrgView>> create(@Valid @RequestBody Organization organization) {
        return orgApiService.create(organization)
                .map(ResponseView::success);
    }

    /**
     * 更新组织。
     *
     * @param orgId            要更新的组织的 ID
     * @param updateOrgRequest 更新组织的请求
     * @return 是否更新成功
     */
    @PutMapping("{orgId}/update")
    public Mono<ResponseView<Boolean>> update(@PathVariable String orgId,
                                              @Valid @RequestBody UpdateOrgRequest updateOrgRequest) {
        return orgApiService.update(orgId, updateOrgRequest)
                .map(ResponseView::success);
    }

    @PostMapping("/{orgId}/logo")
    public Mono<ResponseView<Boolean>> uploadLogo(@PathVariable String orgId,
                                                  @RequestPart("file") Mono<Part> fileMono) {
        return orgApiService.uploadLogo(orgId, fileMono)
                .map(ResponseView::success);
    }

    @DeleteMapping("/{orgId}/logo")
    public Mono<ResponseView<Boolean>> deleteLogo(@PathVariable String orgId) {
        return orgApiService.deleteLogo(orgId)
                .map(ResponseView::success);
    }

    // ...

    /**
     * 获取组织的成员列表。
     *
     * @param orgId 组织的 ID
     * @param page  页码
     * @param count 每页的记录数
     * @return 组织的成员列表的视图
     */
    @GetMapping("/{orgId}/members")
    public Mono<ResponseView<OrgMemberListView>> getOrgMembers(@PathVariable String orgId,
                                                               @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                               @RequestParam(name = "count", required = false, defaultValue = "1000") int count) {
        return orgApiService.getOrganizationMembers(orgId, page, count)
                .map(ResponseView::success);
    }

    /**
     * 更新组织成员的角色。
     *
     * @param updateRoleRequest 包含新角色的请求
     * @param orgId             组织ID
     * @return 包含操作结果的响应视图
     */
    @PutMapping("/{orgId}/role")
    public Mono<ResponseView<Boolean>> updateRoleForMember(@RequestBody UpdateRoleRequest updateRoleRequest,
                                                           @PathVariable String orgId) {
        // 调用orgApiService的updateRoleForMember方法来更新组织成员的角色
        return orgApiService.updateRoleForMember(orgId, updateRoleRequest)
                // 若操作成功，将结果封装在ResponseView中并返回
                .map(ResponseView::success);
    }

    /**
     * 切换当前的组织。
     *
     * @param orgId             要切换到的组织ID
     * @param serverWebExchange 服务器WebExchange
     * @return 包含操作结果的响应视图
     */
    @PutMapping("/switchOrganization/{orgId}")
    public Mono<ResponseView<?>> setCurrentOrganization(@PathVariable String orgId, ServerWebExchange serverWebExchange) {
        // 发布用户登出事件
        return businessEventPublisher.publishUserLogoutEvent()
                // 切换到指定的组织
                .then(orgApiService.switchCurrentOrganizationTo(orgId))
                // 延迟直到发布用户登录事件
                .delayUntil(result -> businessEventPublisher.publishUserLoginEvent(null))
                // 检查组织域并构建响应视图
                .flatMap(result -> orgApiService.checkOrganizationDomain()
                        .flatMap(OrganizationDomainCheckResult::buildOrganizationDomainCheckView)
                        // 如果检查结果为空，返回成功的响应视图
                        .defaultIfEmpty(ResponseView.success(result)));
    }

    /**
     * 删除组织。
     *
     * @param orgId 组织ID。
     * @return 包含删除操作结果的Mono。
     */
    @DeleteMapping("/{orgId}")
    public Mono<ResponseView<Boolean>> removeOrg(@PathVariable String orgId) {
        // 调用orgApiService的removeOrg方法来删除组织
        return orgApiService.removeOrg(orgId)
                // 若操作成功，将结果封装在ResponseView中并返回
                .map(ResponseView::success);
    }

    /**
     * 让当前用户离开组织。
     *
     * @param orgId 要离开的组织ID
     * @return 包含操作结果的响应视图
     */
    @DeleteMapping("/{orgId}/leave")
    public Mono<ResponseView<Boolean>> leaveOrganization(@PathVariable String orgId) {
        // 调用orgApiService的leaveOrganization方法来让当前用户离开组织
        return orgApiService.leaveOrganization(orgId)
                // 若操作成功，将结果封装在ResponseView中并返回
                .map(ResponseView::success);
    }

    /**
     * 从组织中移除指定用户。
     *
     * @param orgId  要操作的组织ID
     * @param userId 要移除的用户ID
     * @return 包含操作结果的响应视图
     */
    @DeleteMapping("/{orgId}/remove")
    public Mono<ResponseView<Boolean>> removeUserFromOrg(@PathVariable String orgId,
                                                         @RequestParam String userId) {
        // 调用orgApiService的removeUserFromOrg方法来从组织中移除指定用户
        return orgApiService.removeUserFromOrg(orgId, userId)
                // 若操作成功，将结果封装在ResponseView中并返回
                .map(ResponseView::success);
    }

    /**
     * 获取所有支持的数据源类型。
     *
     * @param orgId 组织ID
     * @return 包含数据源元信息列表的响应视图
     */
    @GetMapping("/{orgId}/datasourceTypes")
    public Mono<ResponseView<List<DatasourceMetaInfo>>> getSupportedDatasourceTypes(@PathVariable String orgId) {
        // 调用datasourceMetaInfoService的getAllSupportedDatasourceMetaInfos方法来获取所有支持的数据源类型
        return datasourceMetaInfoService.getAllSupportedDatasourceMetaInfos()
                // 若操作成功，将结果封装在ResponseView中并返回
                .collectList()
                .map(ResponseView::success);
    }


    /**
     * 获取组织的通用设置。
     *
     * @param orgId 组织ID
     * @return 包含组织通用设置的响应视图
     */
    @GetMapping("/{orgId}/common-settings")
    public Mono<ResponseView<OrganizationCommonSettings>> getOrgCommonSettings(@PathVariable String orgId) {
        // 调用orgApiService的getOrgCommonSettings方法来获取组织的通用设置
        return orgApiService.getOrgCommonSettings(orgId)
                // 若操作成功，将结果封装在ResponseView中并返回
                .map(ResponseView::success);
    }

    /**
     * 更新组织的通用设置。
     *
     * @param orgId   组织ID
     * @param request 包含要更新的键值对的请求
     * @return 包含操作结果的响应视图
     */
    @PutMapping("/{orgId}/common-settings")
    public Mono<ResponseView<Boolean>> updateOrgCommonSettings(@PathVariable String orgId, @RequestBody UpdateOrgCommonSettingsRequest request) {
        // 调用orgApiService的updateOrgCommonSettings方法来更新组织的通用设置
        return orgApiService.updateOrgCommonSettings(orgId, request.key(), request.value())
                // 若操作成功，将结果封装在ResponseView中并返回
                .map(ResponseView::success);
    }

    /**
     * 一个私有的记录类，用于表示要更新的组织通用设置的键值对。
     */
    private record UpdateOrgCommonSettingsRequest(String key, Object value) {

    }

}