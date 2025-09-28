package com.barda.api.application.view;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 历史快照 DSL 视图类，用于在应用历史中返回应用的 DSL 信息。
 * 该类使用了 Lombok 的 @Getter 和 @Builder 注解来生成 getter 方法和构造器，可以方便地创建该类的实例。
 */
@Getter
@Builder
public class HistorySnapshotDslView {
    /**
     * 应用的 DSL 信息
     */
    private final Map<String, Object> applicationsDsl;
    /**
     * 模块的 DSL 信息
     */
    private final Map<String, Map<String, Object>> moduleDSL;
}