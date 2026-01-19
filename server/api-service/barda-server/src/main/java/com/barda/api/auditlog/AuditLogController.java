package com.barda.api.auditlog;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.PageResponseView;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.home.SessionUserService;
import com.barda.domain.auditlog.model.AuditLog;
import com.barda.domain.auditlog.model.AuditLogSearchRequest;
import com.barda.domain.auditlog.service.AuditLogService;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.event.EventType;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.HasIdAndAuditing;
import com.barda.sdk.util.LocaleUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * 审计日志控制器
 * 提供审计日志的查询接口
 */
@Slf4j
@RestController
@RequestMapping(value = NewUrl.AUDIT_LOG_URL)
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private UserService userService;

    /**
     * 查询审计日志
     *
     * @param request 查询请求
     * @return 分页的审计日志列表
     */
    @PostMapping("/search")
    public Mono<PageResponseView<AuditLog>> search(@Valid @RequestBody AuditLogSearchRequest request) {
        return this.sessionUserService.getVisitorOrgMemberCache()
                .handle((orgMember, synchronousSink) -> {
                    // 检查是否为管理员
                    if (orgMember.isAdmin()) {
                        synchronousSink.next(orgMember);
                    } else {
                        synchronousSink.error(
                                new BizException(
                                        BizError.NOT_AUTHORIZED,
                                        "NOT_AUTHORIZED"
                                )
                        );
                    }
                })
                .flatMap(orgMember ->
                        Mono.zip(
                                this.auditLogService.search(request, ((OrgMember) orgMember).getOrgId()).collectList(),
                                this.auditLogService.count(request, ((OrgMember) orgMember).getOrgId())
                        )
                )
                .zipWhen(tuple -> {
                    // 批量获取用户信息
                    List<AuditLog> auditLogs = tuple.getT1();
                    Set<String> userIds = auditLogs.stream()
                            .map(HasIdAndAuditing::getCreatedBy)
                            .collect(Collectors.toSet());
                    return this.userService.getByIds(userIds);
                })
                .map(tuple -> {
                    // 填充用户信息并返回
                    List<AuditLog> auditLogs = ((Tuple2<List<AuditLog>, Long>) tuple.getT1()).getT1();
                    long total = ((Tuple2<List<AuditLog>, Long>) tuple.getT1()).getT2();
                    Map<String, User> userMap = tuple.getT2();

                    auditLogs.forEach(auditLog ->
                            fillUser(auditLog, userMap.get(auditLog.getCreatedBy()))
                    );

                    return PageResponseView.success(
                            auditLogs,
                            request.getPageNum(),
                            request.getPageSize(),
                            (int) total
                    );
                });
    }

    /**
     * 获取所有事件类型
     *
     * @return 事件类型列表
     */
    @GetMapping("/event-types")
    public Mono<ResponseView<List<Map<String, String>>>> getEventTypes() {
        return Mono.deferContextual(ctx -> {
            Locale locale = LocaleUtils.getLocale(ctx);
            List<Map<String, String>> eventTypes = java.util.Arrays.stream(EventType.values())
                    .map(eventType -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("event", eventType.name());
                        map.put("desc", eventType.getDesc(locale));
                        return map;
                    })
                    .collect(Collectors.toList());
            return Mono.just(ResponseView.success(eventTypes));
        });
    }

    /**
     * 填充用户信息
     *
     * @param auditLog 审计日志
     * @param user     用户信息
     */
    private void fillUser(AuditLog auditLog, User user) {
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            auditLog.setUser(userInfo);
        }
    }
}
