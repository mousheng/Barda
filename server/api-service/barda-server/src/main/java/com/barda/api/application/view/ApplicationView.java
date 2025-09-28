package com.barda.api.application.view;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 应用视图类，用于在应用列表中返回应用的完整信息。
 * 该类使用了 Lombok 的 @Builder 注解来生成构造器，可以方便地创建该类的实例。
 */
@Builder
@Getter
public class ApplicationView {
    /**
     * 应用的基础信息
     */
    private final ApplicationInfoView applicationInfoView;
    /**
     * 应用的 DSL 信息
     */
    private final Map<String, Object> applicationDSL;
    /**
     * 模块的 DSL 信息
     */
    private final Map<String, Map<String, Object>> moduleDSL;
    /**
     * 组织的通用设置
     */
    private final Map<String, Object> orgCommonSettings;
    /**
     * 模板ID
     */
    private final String templateId;
}
