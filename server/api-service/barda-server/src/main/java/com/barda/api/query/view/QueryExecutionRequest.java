package com.barda.api.query.view;

import static com.barda.sdk.util.StreamUtils.toMapNullFriendly;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.barda.domain.query.model.LibraryQueryCombineId;
import com.barda.sdk.models.Param;

import lombok.Setter;

/**
 * 查询执行请求类。
 */
@Setter
public class QueryExecutionRequest {

    /**
     * 应用 ID。
     */
    private String applicationId;

    /**
     * 查询 ID。
     */
    private String queryId;

    /**
     * 库查询 ID。
     */
    private String libraryQueryId;

    /**
     * 库查询记录 ID。
     */
    private String libraryQueryRecordId;

    /**
     * 查询参数列表。
     */
    private List<Param> params;

    /**
     * 是否为查看模式。
     * 默认为 false。
     */
    private boolean viewMode = false;

    /**
     * 路径。
     */
    private String[] path;

    /**
     * 获取应用 ID。
     *
     * @return 应用 ID
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * 获取查询 ID。
     *
     * @return 查询 ID
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * 获取是否为查看模式。
     *
     * @return true 为查看模式，false 否则
     */
    public boolean isViewMode() {
        return viewMode;
    }

    /**
     * 获取路径。
     *
     * @return 路径
     */
    public String[] getPath() {
        return path;
    }

    /**
     * 获取查询参数的键值对映射。
     *
     * 键为参数的键，值为空格或 null 则不包含在映射中。
     * 若存在键相同的情况，则取最后一个参数的值。
     *
     * @return 查询参数的键值对映射
     */
    public Map<String, Object> paramMap() {
        return emptyIfNull(params).stream()
                .filter(it -> StringUtils.isNotBlank(it.getKey()))
                .collect(toMapNullFriendly(param -> param.getKey().trim(), Param::getValue, (a, b) -> b));
    }

    /**
     * 判断是否为应用查询请求。
     *
     * @return true 为应用查询请求，false 否则
     */
    public boolean isApplicationQueryRequest() {
        return StringUtils.isNoneBlank(queryId, applicationId);
    }

    /**
     * 获取库查询的组合 ID。
     *
     * @return 库查询的组合 ID
     */
    public LibraryQueryCombineId getLibraryQueryCombineId() {
        return new LibraryQueryCombineId(libraryQueryId, libraryQueryRecordId);
    }
}
