package com.barda.domain.util;

import com.querydsl.core.types.Path;

/**
 * Query DSL 实用工具类。
 * 该类提供了一组静态方法来帮助在 Query DSL 中使用字段名称。
 */
public class QueryDslUtils {

    /**
     * 获取路径对象的字段名称。
     *
     * @param path 路径对象
     * @return 字段名称，如果路径对象为 null，则返回 null
     */
    public static String fieldName(Path<?> path) {
        return path != null ? path.getMetadata().getName() : null;
    }
}
