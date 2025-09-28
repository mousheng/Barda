package com.barda.sdk.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.barda.sdk.exception.PluginError;
import com.barda.sdk.exception.PluginException;

import lombok.Getter;

/**
 * 一个表示查询执行结果的类。
 * 它使用Lombok库的@JsonInclude和@Getter注解来自动生成JSON序列化和getter方法。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class QueryExecutionResult {

    private static final String CODE_FAILED = "FAILED";
    private static final String CODE_OK = "OK";

    /**
     * 查询的状态代码。
     */
    private String queryCode = CODE_OK;

    /**
     * 查询结果的头部信息。
     */
    private JsonNode headers;

    /**
     * 查询结果的数据。
     */
    private Object data;

    /**
     * 本地化消息的键。
     */
    @JsonIgnore
    private String messageKey;

    /**
     * 本地化消息的参数。
     */
    @JsonIgnore
    private Object[] messageArgs;

    /**
     * 提示信息的本地化消息列表。
     */
    @JsonIgnore
    private List<LocaleMessage> hintLocaleMessages = new ArrayList<>();

    /**
     * 查询结果的消息。
     */
    private String message;

    /**
     * 私有的构造函数，防止直接创建QueryExecutionResult实例。
     */
    private QueryExecutionResult() {
    }

    /**
     * 创建一个表示成功的QueryExecutionResult实例。
     *
     * @param data 查询结果的数据
     * @return 一个新的QueryExecutionResult实例
     */
    public static QueryExecutionResult success(Object data) {
        QueryExecutionResult result = new QueryExecutionResult();
        result.data = data;
        return result;
    }

    /**
     * 创建一个表示成功的QueryExecutionResult实例，并包含提示信息的本地化消息列表。
     *
     * @param data 查询结果的数据
     * @param hintLocalMessages 提示信息的本地化消息列表
     * @return 一个新的QueryExecutionResult实例
     */
    public static QueryExecutionResult success(Object data, List<LocaleMessage> hintLocalMessages) {
        QueryExecutionResult result = new QueryExecutionResult();
        result.data = data;
        result.hintLocaleMessages = hintLocalMessages;
        return result;
    }

    /**
     * 创建一个表示错误的QueryExecutionResult实例，并包含PluginException。
     *
     * @param exception 包含错误信息的PluginException
     * @return 一个新的QueryExecutionResult实例
     */
    public static QueryExecutionResult error(PluginException exception) {
        return error(exception.getError(), exception.getMessageKey(), exception.getArgs());
    }

    /**
     * 创建一个表示错误的QueryExecutionResult实例。
     *
     * @param bizError 包含错误信息的PluginError
     * @param messageKey 本地化消息的键
     * @param args 本地化消息的参数
     * @return 一个新的QueryExecutionResult实例
     */
    public static QueryExecutionResult error(PluginError bizError, String messageKey, Object... args) {
        QueryExecutionResult result = new QueryExecutionResult();
        result.queryCode = bizError.name();
        result.messageKey = messageKey;
        result.messageArgs = args;
        return result;
    }

    /**
     * 创建一个表示错误的QueryExecutionResult实例。
     *
     * @param bizError 包含错误信息的PluginError
     * @param messageKey 本地化消息的键
     * @return 一个新的QueryExecutionResult实例
     */
    public static QueryExecutionResult error(PluginError bizError, String messageKey) {
        QueryExecutionResult result = new QueryExecutionResult();
        result.queryCode = bizError.name();
        result.messageKey = messageKey;
        return result;
    }

    /**
     * 创建一个表示错误的QueryExecutionResult实例，并包含自定义的消息。
     *
     * @param message 自定义的消息
     * @return 一个新的QueryExecutionResult实例
     */
    public static QueryExecutionResult errorWithMessage(String message) {
        QueryExecutionResult result = new QueryExecutionResult();
        result.queryCode = CODE_FAILED;
        result.message = message;
        return result;
    }

    /**
     * 创建一个表示REST API结果的QueryExecutionResult实例。
     *
     * @param httpStatus HTTP状态码
     * @param resultHeaders 查询结果的头部信息
     * @param body 查询结果的数据
     * @return 一个新的QueryExecutionResult实例
     */
    public static QueryExecutionResult ofRestApiResult(HttpStatus httpStatus, JsonNode resultHeaders, Object body) {
        QueryExecutionResult result = new QueryExecutionResult();
        if (!httpStatus.is2xxSuccessful()) {
            result.queryCode = "HTTP" + httpStatus.name();
        }
        result.headers = resultHeaders;
        result.data = body;
        return result;
    }

    /**
     * 判断查询执行结果是否表示成功。
     *
     * @return 如果查询执行结果表示成功，返回true；否则返回false
     */
    public boolean isSuccess() {
        return queryCode.equals(CODE_OK);
    }

    /**
     * 获取本地化消息。
     *
     * @return 如果存在本地化消息的键，返回一个新的LocaleMessage实例；否则返回null
     */
    @JsonIgnore
    public LocaleMessage getLocaleMessage() {
        if (StringUtils.isBlank(messageKey)) {
            return null;
        }
        return new LocaleMessage(messageKey, messageArgs);
    }

    /**
     * 获取提示信息的本地化消息列表。
     *
     * @return 提示信息的本地化消息列表
     */
    public List<LocaleMessage> getHintLocaleMessages() {
        return hintLocaleMessages;
    }
}
