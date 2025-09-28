package com.barda.api.query.view;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 用于执行 upsert 库查询的请求类。
 */
@Getter
@Setter
public class UpsertLibraryQueryRequest {

    /**
     * 库名称。
     */
    private String name;

    /**
     * 库查询 DSL。
     * 键为查询条件的名称，值为查询条件的值。
     */
    private Map<String, Object> libraryQueryDSL;

}