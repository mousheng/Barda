package com.barda.api.query;

import static com.barda.infra.constant.NewUrl.LIBRARY_QUERY_RECORD_URL;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.ResponseView;
import com.barda.api.query.view.LibraryQueryRecordMetaView;
import com.barda.domain.query.model.LibraryQueryCombineId;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = LIBRARY_QUERY_RECORD_URL)
public class LibraryQueryRecordController {

    @Autowired
    private LibraryQueryRecordApiService libraryQueryRecordApiService;

    /**
     * 删除库查询记录。
     *
     * @param libraryQueryRecordId 库查询记录 ID
     * @return 空的 Mono 对象
     */
    @DeleteMapping("/{libraryQueryRecordId}")
    public Mono<Void> delete(@PathVariable String libraryQueryRecordId) {
        return libraryQueryRecordApiService.delete(libraryQueryRecordId);
    }

    /**
     * 获取库查询记录列表。
     *
     * @param libraryQueryId 库查询 ID
     * @return 库查询记录元视图列表的 Mono 对象
     */
    @GetMapping("/listByLibraryQueryId")
    public Mono<ResponseView<List<LibraryQueryRecordMetaView>>> getByLibraryQueryId(@RequestParam(name = "libraryQueryId") String libraryQueryId) {
        return libraryQueryRecordApiService.getByLibraryQueryId(libraryQueryId)
                .map(ResponseView::success);
    }

    /**
     * 获取库查询记录的 DSL。
     *
     * @param libraryQueryId 库查询 ID
     * @param libraryQueryRecordId 库查询记录 ID
     * @return DSL 值的 Mono 对象
     */
    @GetMapping
    public Mono<ResponseView<Map<String, Object>>> dslById(@RequestParam(name = "libraryQueryId") String libraryQueryId,
            @RequestParam(name = "libraryQueryRecordId") String libraryQueryRecordId) {
        LibraryQueryCombineId libraryQueryCombineId = new LibraryQueryCombineId(libraryQueryId, libraryQueryRecordId);
        return libraryQueryRecordApiService.getRecordDSLFromLibraryQueryCombineId(libraryQueryCombineId)
                .map(ResponseView::success);
    }
}
