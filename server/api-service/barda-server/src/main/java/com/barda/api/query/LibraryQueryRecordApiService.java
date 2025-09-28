package com.barda.api.query;

import static com.barda.api.util.ViewBuilder.multiBuild;
import static com.barda.sdk.exception.BizError.LIBRARY_QUERY_AND_ORG_NOT_MATCH;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserService;
import com.barda.api.query.view.LibraryQueryRecordMetaView;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.query.model.LibraryQuery;
import com.barda.domain.query.model.LibraryQueryCombineId;
import com.barda.domain.query.model.LibraryQueryRecord;
import com.barda.domain.query.service.LibraryQueryRecordService;
import com.barda.domain.query.service.LibraryQueryService;
import com.barda.domain.user.service.UserService;

import reactor.core.publisher.Mono;

@Service
public class LibraryQueryRecordApiService {

    @Autowired
    private LibraryQueryService libraryQueryService;

    @Autowired
    private LibraryQueryRecordService libraryQueryRecordService;

    @Autowired
    private LibraryQueryApiService libraryQueryApiService;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private OrgDevChecker orgDevChecker;

    @Autowired
    private UserService userService;

    /**
     * 获取库查询记录的 DSL。
     *
     * @param libraryQueryCombineId 库查询组合 ID
     * @return DSL 值的 Mono 对象
     */
    public Mono<Map<String, Object>> getRecordDSLFromLibraryQueryCombineId(LibraryQueryCombineId libraryQueryCombineId) {
        return libraryQueryApiService.checkLibraryQueryViewPermission(libraryQueryCombineId.libraryQueryId())
                .then(checkLibraryQueryRecordViewPermission(libraryQueryCombineId))
                .then(Mono.defer(() -> {
                    if (libraryQueryCombineId.isUsingLiveRecord()) {
                        return libraryQueryService.getLiveDSLByLibraryQueryId(libraryQueryCombineId.libraryQueryId());
                    }
                    return libraryQueryRecordService.getById(libraryQueryCombineId.libraryQueryRecordId())
                            .map(LibraryQueryRecord::getLibraryQueryDSL);
                }));
    }

    /**
     * 删除库查询记录。
     *
     * @param id 库查询记录 ID
     * @return 空的 Mono 对象
     */
    public Mono<Void> delete(String id) {
        return checkLibraryQueryRecordManagementPermission(id)
                .then(libraryQueryRecordService.deleteById(id));
    }

    /**
     * 获取库查询记录列表。
     *
     * @param libraryQueryId 库查询 ID
     * @return 库查询记录元视图列表的 Mono 对象
     */
    public Mono<List<LibraryQueryRecordMetaView>> getByLibraryQueryId(String libraryQueryId) {
        return libraryQueryApiService.checkLibraryQueryManagementPermission(libraryQueryId)
                .then(libraryQueryRecordService.getByLibraryQueryId(libraryQueryId))
                .flatMap(libraryQueryRecords -> multiBuild(libraryQueryRecords,
                        LibraryQueryRecord::getCreatedBy,
                        userService::getByIds,
                        LibraryQueryRecordMetaView::from
                ));
    }


    /**
     * 检查库查询记录的管理权限。
     *
     * @param libraryQueryRecordId 库查询记录 ID
     * @return 空的 Mono 对象
     */
    Mono<Void> checkLibraryQueryRecordManagementPermission(String libraryQueryRecordId) {
        return orgDevChecker.checkCurrentOrgDev()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .zipWith(libraryQueryRecordService.getById(libraryQueryRecordId)
                        .flatMap(libraryQueryRecord -> libraryQueryService.getById(libraryQueryRecord.getLibraryQueryId())))
                .flatMap(tuple2 -> {
                    OrgMember orgMember = tuple2.getT1();
                    LibraryQuery libraryQuery = tuple2.getT2();
                    if (!orgMember.getOrgId().equals(libraryQuery.getOrganizationId())) {
                        return ofError(LIBRARY_QUERY_AND_ORG_NOT_MATCH, "LIBRARY_QUERY_AND_ORG_NOT_MATCH");
                    }
                    return Mono.empty();
                });
    }

    /**
     * 检查库查询记录的视图权限。
     *
     * @param libraryQueryCombineId 库查询组合 ID
     * @return 空的 Mono 对象
     */
    Mono<Void> checkLibraryQueryRecordViewPermission(LibraryQueryCombineId libraryQueryCombineId) {
        return sessionUserService.getVisitorOrgMemberCache()
                .zipWith(Mono.defer(() -> {
                    if (libraryQueryCombineId.isUsingLiveRecord()) {
                        return libraryQueryService.getById(libraryQueryCombineId.libraryQueryId());
                    }
                    return libraryQueryRecordService.getById(libraryQueryCombineId.libraryQueryRecordId())
                            .flatMap(libraryQueryRecord -> libraryQueryService.getById(libraryQueryRecord.getLibraryQueryId()));

                }))
                .flatMap(tuple2 -> {
                    OrgMember orgMember = tuple2.getT1();
                    LibraryQuery libraryQuery = tuple2.getT2();
                    if (!orgMember.getOrgId().equals(libraryQuery.getOrganizationId())) {
                        return ofError(LIBRARY_QUERY_AND_ORG_NOT_MATCH, "LIBRARY_QUERY_AND_ORG_NOT_MATCH");
                    }
                    return Mono.empty();
                });
    }
}
