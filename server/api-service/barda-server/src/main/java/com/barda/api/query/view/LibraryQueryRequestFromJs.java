package com.barda.api.query.view;

import static com.barda.sdk.util.StreamUtils.toMapNullFriendly;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.models.Param;

import lombok.Getter;
import lombok.Setter;

/**
 * 用于从 JavaScript 端接收库查询请求的类。
 */
@Setter
@Getter
public class LibraryQueryRequestFromJs {

    /**
     * 库查询名称。
     */
    private String libraryQueryName;

    /**
     * 库查询记录 ID。
     */
    private String libraryQueryRecordId;

    /**
     * 查询参数列表。
     */
    private List<Param> params;

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

}
