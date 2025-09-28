package com.barda.infra.serverlog;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Getter;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * 服务器日志实体类。
 * 该类使用了Lombok库来自动生成getter方法和构建器。
 * 该类使用了Spring Data MongoDB来映射到MongoDB的集合中。
 */
@Document
@Getter
@Builder
public class ServerLog {

    /**
     * 用户ID。
     */
    private String userId;

    /**
     * 请求的URL路径。
     */
    private String urlPath;

    /**
     * HTTP方法。
     */
    private String httpMethod;

    /**
     * 请求的主体。
     */
    private String requestBody;

    /**
     * 查询参数。
     */
    private Map<String, String> queryParameters;

    /**
     * 创建时间。
     */
    private long createTime;

    /**
     * 私有构造函数，用于JSON反序列化。
     *
     * @param userId 用户ID
     * @param urlPath 请求的URL路径
     * @param httpMethod HTTP方法
     * @param requestBody 请求的主体
     * @param queryParameters 查询参数
     * @param createTime 创建时间
     */
    @JsonCreator
    private ServerLog(String userId, String urlPath, String httpMethod, String requestBody, Map<String, String> queryParameters, long createTime) {
        this.userId = userId;
        this.urlPath = urlPath;
        this.createTime = createTime;
        this.httpMethod = httpMethod;
        this.requestBody = requestBody;
        this.queryParameters = queryParameters;
    }
}
