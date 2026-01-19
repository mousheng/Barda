package com.barda.api.query;

import static com.barda.infra.constant.NewUrl.LIBRARY_QUERY_URL;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.ResponseView;
import com.barda.api.query.view.LibraryQueryAggregateView;
import com.barda.api.query.view.LibraryQueryPublishRequest;
import com.barda.api.query.view.LibraryQueryRecordMetaView;
import com.barda.api.query.view.LibraryQueryView;
import com.barda.api.query.view.UpsertLibraryQueryRequest;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.query.model.LibraryQuery;
import com.barda.domain.query.service.LibraryQueryService;
import com.barda.infra.event.EventType;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = LIBRARY_QUERY_URL)
public class LibraryQueryController {

    @Autowired
    private LibraryQueryService libraryQueryService;
    @Autowired
    private LibraryQueryApiService libraryQueryApiService;
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 获取库查询下拉列表。
     *
     * @return 库查询聚合视图列表的 Mono 对象
     */
    @GetMapping("/dropDownList")
    public Mono<ResponseView<List<LibraryQueryAggregateView>>> dropDownList() {
        return libraryQueryApiService.dropDownList()
                .map(ResponseView::success);
    }

    /**
     * 获取库查询列表。
     *
     * @return 库查询视图列表的 Mono 对象
     */
    @GetMapping("/listByOrg")
    public Mono<ResponseView<List<LibraryQueryView>>> list() {
        return libraryQueryApiService.listLibraryQueries()
                .map(ResponseView::success);
    }

    /**
     * 创建库查询。
     *
     * @param libraryQuery 库查询
     * @return 库查询视图的 Mono 对象
     */
    @PostMapping
    public Mono<ResponseView<LibraryQueryView>> create(@RequestBody LibraryQuery libraryQuery) {
        return libraryQueryApiService.create(libraryQuery)
                .delayUntil(libraryQueryView ->
                        businessEventPublisher.publishLibraryQueryEvent(libraryQueryView.id(), libraryQueryView.name(),
                                EventType.LIBRARY_QUERY_CREATE))
                .map(ResponseView::success);
    }

    /**
     * 更新库查询。
     *
     * @param libraryQueryId 库查询 ID
     * @param upsertLibraryQueryRequest 更新库查询请求
     * @return 布尔值的 Mono 对象，表示是否更新成功
     */
    @PutMapping("/{libraryQueryId}")
    public Mono<ResponseView<Boolean>> update(@PathVariable String libraryQueryId,
            @RequestBody UpsertLibraryQueryRequest upsertLibraryQueryRequest) {
        return libraryQueryService.getById(libraryQueryId)
                .flatMap(libraryQuery ->
                        libraryQueryApiService.update(libraryQueryId, upsertLibraryQueryRequest)
                                .delayUntil(result -> businessEventPublisher.publishLibraryQueryEvent(
                                        libraryQuery.getId(),
                                        libraryQuery.getName(),
                                        EventType.LIBRARY_QUERY_UPDATE))
                )
                .map(ResponseView::success);
    }

    /**
     * 删除库查询。
     *
     * @param libraryQueryId 库查询 ID
     * @return 布尔值的 Mono 对象，表示是否删除成功
     */
    @DeleteMapping("/{libraryQueryId}")
    public Mono<ResponseView<Boolean>> delete(@PathVariable String libraryQueryId) {
        return libraryQueryService.getById(libraryQueryId)
                .delayUntil(__ -> libraryQueryApiService.delete(libraryQueryId))
                .delayUntil(libraryQuery -> businessEventPublisher.publishLibraryQueryEvent(libraryQuery.getId(), libraryQuery.getName(),
                        EventType.LIBRARY_QUERY_DELETE))
                .thenReturn(ResponseView.success(true));
    }

    /**
     * 发布库查询。
     *
     * @param libraryQueryId 库查询 ID
     * @param libraryQueryPublishRequest 库查询发布请求
     * @return 库查询记录元视图的 Mono 对象
     */
    @PostMapping("/{libraryQueryId}/publish")
    public Mono<ResponseView<LibraryQueryRecordMetaView>> publish(@PathVariable String libraryQueryId,
            @RequestBody LibraryQueryPublishRequest libraryQueryPublishRequest) {
        return libraryQueryApiService.publish(libraryQueryId, libraryQueryPublishRequest)
                .delayUntil(__ -> libraryQueryService.getById(libraryQueryId)
                        .flatMap(libraryQuery -> businessEventPublisher.publishLibraryQuery(libraryQuery, EventType.LIBRARY_QUERY_PUBLISH)))
                .map(ResponseView::success);
    }
}
