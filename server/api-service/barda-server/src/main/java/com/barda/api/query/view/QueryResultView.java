package com.barda.api.query.view;

import java.util.Collection;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.util.LocaleUtils;

/**
 * 查询结果视图类。
 */
public class QueryResultView {

    /**
     * 查询执行结果。
     */
    private final QueryExecutionResult queryResult;

    /**
     * 本地化信息。
     */
    private final Locale locale;

    /**
     * 构造函数。
     *
     * @param queryResult 查询执行结果
     * @param locale 本地化信息
     */
    public QueryResultView(QueryExecutionResult queryResult, Locale locale) {
        this.queryResult = queryResult;
        this.locale = locale;
    }

    /**
     * 获取查询结果状态码。
     *
     * 1 表示服务器端处理当前查询成功。
     *
     * @return 查询结果状态码
     */
    public int getCode() {
        return 1;
    }

    /**
     * 获取查询代码。
     *
     * @return 查询代码
     */
    public String getQueryCode() {
        return queryResult.getQueryCode();
    }

    /**
     * 查询是否成功。
     *
     * @return true 为成功，false 为失败
     */
    public boolean isSuccess() {
        return queryResult.isSuccess();
    }

    /**
     * 获取查询结果的头部信息。
     *
     * @return 查询结果的头部信息
     */
    public JsonNode getHeaders() {
        return queryResult.getHeaders();
    }

    /**
     * 获取查询结果的数据。
     *
     * @return 查询结果的数据
     */
    public Object getData() {
        return queryResult.getData();
    }

    /**
     * 获取查询结果的消息。
     *
     * 如果存在本地化的消息，则使用本地化的消息，否则使用原始的消息。
     *
     * @return 查询结果的消息
     */
    public String getMessage() {
        return queryResult.getLocaleMessage() != null ? LocaleUtils.getMessage(locale, queryResult.getLocaleMessage()) : queryResult.getMessage();
    }

    /**
     * 获取查询结果的提示消息。
     *
     * 若存在本地化的提示消息，则使用本地化的提示消息，否则使用原始的提示消息。
     *
     * @return 查询结果的提示消息
     */
    public Collection<String> getHintMessages() {
        return queryResult.getHintLocaleMessages()
                .stream()
                .map(msg -> LocaleUtils.getMessage(locale, msg))
                .toList();
    }
}
