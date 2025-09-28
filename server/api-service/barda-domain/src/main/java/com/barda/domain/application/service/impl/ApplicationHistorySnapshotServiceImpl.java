package com.barda.domain.application.service.impl;

import static com.barda.sdk.exception.BizError.INVALID_HISTORY_SNAPSHOT;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofException;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.barda.domain.application.model.ApplicationHistorySnapshot;
import com.barda.domain.application.repository.ApplicationHistorySnapshotRepository;
import com.barda.domain.application.service.ApplicationHistorySnapshotService;
import com.barda.sdk.exception.BizError;

import reactor.core.publisher.Mono;

@Lazy
@Service
public class ApplicationHistorySnapshotServiceImpl implements ApplicationHistorySnapshotService {

    @Autowired
    private ApplicationHistorySnapshotRepository repository;

    @Override
    public Mono<Boolean> createHistorySnapshot(String applicationId, Map<String, Object> dsl, Map<String, Object> context, String userId) {
        ApplicationHistorySnapshot applicationHistorySnapshot = new ApplicationHistorySnapshot();
        applicationHistorySnapshot.setApplicationId(applicationId);
        applicationHistorySnapshot.setDsl(dsl);
        applicationHistorySnapshot.setContext(context);
        return repository.save(applicationHistorySnapshot)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    @Override
    public Mono<List<ApplicationHistorySnapshot>> listAllHistorySnapshotBriefInfo(String applicationId, PageRequest pageRequest) {
        return repository.findAllByApplicationId(applicationId, pageRequest.withSort(Direction.DESC, "id"))
                .collectList()
                .onErrorMap(Exception.class, e -> ofException(BizError.FETCH_HISTORY_SNAPSHOT_FAILURE, "FETCH_HISTORY_SNAPSHOT_FAILURE"));
    }

    @Override
    public Mono<Long> countByApplicationId(String applicationId) {
        return repository.countByApplicationId(applicationId)
                .onErrorMap(Exception.class,
                        e -> ofException(BizError.FETCH_HISTORY_SNAPSHOT_COUNT_FAILURE, "FETCH_HISTORY_SNAPSHOT_COUNT_FAILURE"));
    }


    @Override
    public Mono<ApplicationHistorySnapshot> getHistorySnapshotDetail(String historySnapshotId) {
        return repository.findById(historySnapshotId)
                .switchIfEmpty(deferredError(INVALID_HISTORY_SNAPSHOT, "INVALID_HISTORY_SNAPSHOT", historySnapshotId));
    }
}
