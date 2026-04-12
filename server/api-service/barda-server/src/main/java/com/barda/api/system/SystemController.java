package com.barda.api.system;

import static com.barda.infra.constant.NewUrl.PREFIX;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.ResponseView;
import com.barda.domain.organization.model.Organization;
import com.barda.api.organization.PrimaryOrgApiService;

import lombok.Builder;
import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * 系统管理控制器。
 */
@RestController
@RequestMapping(PREFIX + "/system")
public class SystemController {

    @Autowired
    private PrimaryOrgApiService primaryOrgService;

    /**
     * 获取主要组织信息。
     *
     * @return 主要组织视图
     */
    @GetMapping("/primary-organization")
    public Mono<ResponseView<PrimaryOrgView>> getPrimaryOrganization() {
        return primaryOrgService.getPrimaryOrg()
                .map(org -> ResponseView.success(PrimaryOrgView.from(org)));
    }

    /**
     * 设置主要组织。
     * 只有当前主要组织的管理员可以执行此操作。
     *
     * @param orgId 新的主要组织 ID
     * @return 空响应
     */
    @PostMapping("/primary-organization/{orgId}")
    public Mono<ResponseView<Void>> setPrimaryOrganization(@PathVariable String orgId) {
        return primaryOrgService.setPrimaryOrganization(orgId)
                .thenReturn(ResponseView.success(null));
    }

    /**
     * 检查当前用户是否是主要组织的管理员。
     *
     * @return 是否是主要组织管理员
     */
    @GetMapping("/is-primary-org-admin")
    public Mono<ResponseView<Boolean>> isCurrentUserPrimaryOrgAdmin() {
        return primaryOrgService.isCurrentUserPrimaryOrgAdmin()
                .map(ResponseView::success);
    }

    /**
     * 主要组织视图。
     */
    @Getter
    @Builder
    public static class PrimaryOrgView {
        /**
         * 组织 ID。
         */
        private String id;

        /**
         * 组织名称。
         */
        private String name;

        /**
         * 是否为主要组织。
         */
        private Boolean isPrimary;

        /**
         * 创建时间。
         */
        private Long createdAt;

        /**
         * 从 Organization 创建视图。
         *
         * @param org 组织
         * @return 主要组织视图
         */
        public static PrimaryOrgView from(Organization org) {
            return PrimaryOrgView.builder()
                    .id(org.getId())
                    .name(org.getName())
                    .isPrimary(org.isPrimary())
                    .createdAt(org.getCreatedAt() != null ?
                            org.getCreatedAt().toEpochMilli() : null)
                    .build();
        }
    }
}
