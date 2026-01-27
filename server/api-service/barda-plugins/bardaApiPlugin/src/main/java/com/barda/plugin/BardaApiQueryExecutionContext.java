package com.barda.plugin;

import org.springframework.http.HttpCookie;
import org.springframework.util.MultiValueMap;

import com.barda.sdk.query.QueryExecutionContext;

import lombok.Builder;
import lombok.Getter;

/**
 * BardaApiQueryExecutionContext 类是 QueryExecutionContext 的子类，
 * 用于表示在执行与 Barda API 的查询操作时所需要的上下文。
 */
@Builder
@Getter
public class BardaApiQueryExecutionContext extends QueryExecutionContext {

    /**
     * 应用的端口号。
     */
    private int port;

    /**
     * 查询操作的类型。
     */
    private String actionType;

    /**
     * 访问者的 ID。
     */
    private String visitorId;

    /**
     * 应用的组织 ID。
     */
    private String applicationOrgId;

    /**
     * 在查询操作中需要发送的 Cookie。
     */
    private MultiValueMap<String, HttpCookie> requestCookies;

    /**
     * 文件夹名称或 ID。
     */
    private String folderName;
}
