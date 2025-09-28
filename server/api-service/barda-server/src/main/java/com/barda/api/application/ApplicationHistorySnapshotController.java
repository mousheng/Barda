package com.barda.api.application;

import static com.barda.api.util.ViewBuilder.multiBuild;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.barda.api.application.view.HistorySnapshotDslView;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.home.SessionUserService;
import com.barda.api.util.Pagination;
import com.barda.domain.application.model.Application;
import com.barda.domain.application.model.ApplicationHistorySnapshot;
import com.barda.domain.application.service.ApplicationHistorySnapshotService;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.user.service.UserService;
import com.barda.infra.constant.NewUrl;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 应用历史快照控制器类，提供应用历史快照相关的API。
 */
@Slf4j
@RestController
@RequestMapping(value = {NewUrl.APPLICATION_HISTORY_URL})
public class ApplicationHistorySnapshotController {

    /**
     * 资源权限服务。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 应用历史快照服务。
     */
    @Autowired
    private ApplicationHistorySnapshotService applicationHistorySnapshotService;

    /**
     * 会话用户服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 应用服务。
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * 创建应用历史快照。
     *
     * @param request 创建应用历史快照的请求。
     * @return 一个Mono，发出包含布尔值的ResponseView。
     */
    @PostMapping
    public Mono<ResponseView<Boolean>> create(@RequestBody ApplicationHistorySnapshotRequest request) {
        return sessionUserService.getVisitorId()
                .delayUntil(visitor -> resourcePermissionService.checkResourcePermissionWithError(visitor, request.applicationId(),
                        ResourceAction.EDIT_APPLICATIONS))
                .flatMap(visitorId -> applicationHistorySnapshotService.createHistorySnapshot(request.applicationId(),
                        request.dsl(),
                        request.context(),
                        visitorId)
                )
                .map(ResponseView::success);
    }

    /**
     * 获取应用的所有历史快照的简要信息。
     *
     * @param applicationId 应用ID。
     * @param page          页码。
     * @param size          每页的大小。
     * @return 一个Mono，发出包含应用历史快照简要信息列表和总数的ResponseView。
     */
    @GetMapping("/{applicationId}")
    public Mono<ResponseView<Map<String, Object>>> listAllHistorySnapshotBriefInfo(@PathVariable String applicationId,
                                                                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        Pagination pagination = Pagination.of(page, size).check();

        return sessionUserService.getVisitorId()
                .delayUntil(visitor -> resourcePermissionService.checkResourcePermissionWithError(visitor, applicationId,
                        ResourceAction.EDIT_APPLICATIONS))
                .flatMap(__ -> applicationHistorySnapshotService.listAllHistorySnapshotBriefInfo(applicationId,
                        pagination.toPageRequest()))
                .flatMap(snapshotList -> {
                    Mono<List<ApplicationHistorySnapshotBriefInfo>> snapshotBriefInfoList = multiBuild(snapshotList,
                            ApplicationHistorySnapshot::getCreatedBy,
                            userService::getByIds,
                            (applicationHistorySnapshot, user) -> new ApplicationHistorySnapshotBriefInfo(
                                    applicationHistorySnapshot.getId(),
                                    applicationHistorySnapshot.getContext(),
                                    applicationHistorySnapshot.getCreatedBy(),
                                    user.getName(),
                                    user.getAvatarUrl(),
                                    applicationHistorySnapshot.getCreatedAt().toEpochMilli()
                            )
                    );

                    Mono<Long> applicationHistorySnapshotCount = applicationHistorySnapshotService.countByApplicationId(applicationId);

                    return Mono.zip(snapshotBriefInfoList, applicationHistorySnapshotCount)
                            .map(tuple -> ImmutableMap.of("list", tuple.getT1(), "count", tuple.getT2()));
                })
                .map(ResponseView::success);
    }

    /**
     * 获取应用的指定历史快照的DSL。
     *
     * @param applicationId 应用ID。
     * @param snapshotId    快照ID。
     * @return 一个Mono，发出包含历史快照DSL视图的ResponseView。
     */
    @GetMapping("/{applicationId}/{snapshotId}")
    public Mono<ResponseView<HistorySnapshotDslView>> getHistorySnapshotDsl(@PathVariable String applicationId,
                                                                            @PathVariable String snapshotId) {
        return sessionUserService.getVisitorId()
                .delayUntil(visitor -> resourcePermissionService.checkResourcePermissionWithError(visitor, applicationId,
                        ResourceAction.EDIT_APPLICATIONS))
                .flatMap(__ -> applicationHistorySnapshotService.getHistorySnapshotDetail(snapshotId))
                .map(ApplicationHistorySnapshot::getDsl)
                .zipWhen(dsl -> applicationService.getAllDependentModulesFromDsl(dsl))
                .map(tuple -> {
                    Map<String, Object> applicationDsl = tuple.getT1();
                    List<Application> dependentModules = tuple.getT2();
                    Map<String, Map<String, Object>> dependentModuleDsl = dependentModules.stream()
                            .collect(Collectors.toMap(Application::getId, Application::getLiveApplicationDsl, (a, b) -> b));
                    return HistorySnapshotDslView.builder()
                            .applicationsDsl(applicationDsl)
                            .moduleDSL(dependentModuleDsl)
                            .build();
                })
                .map(ResponseView::success);
    }

    /**
     * 应用程序历史快照摘要信息。
     *
     * @param snapshotId 快照ID
     * @param context    上下文信息
     * @param userId     用户ID
     * @param userName   用户名
     * @param userAvatar 用户头像
     * @param createTime 创建时间
     */
    private record ApplicationHistorySnapshotBriefInfo(String snapshotId, Map<String, Object> context,
                                                       String userId, String userName,
                                                       String userAvatar, long createTime) {
    }

    /**
     * 应用程序历史快照请求信息。
     *
     * @param applicationId 应用程序ID
     * @param dsl           DSL信息
     * @param context       上下文信息
     */
    private record ApplicationHistorySnapshotRequest(String applicationId, Map<String, Object> dsl, Map<String, Object> context) {
    }


}
