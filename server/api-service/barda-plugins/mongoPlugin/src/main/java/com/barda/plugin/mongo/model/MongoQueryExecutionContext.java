package com.barda.plugin.mongo.model;

import org.bson.Document;

import com.barda.sdk.query.QueryExecutionContext;

import lombok.Builder;

/**
 * 扩展自 {@link QueryExecutionContext}，为 MongoDB 查询提供执行上下文。
 */
@Builder
public class MongoQueryExecutionContext extends QueryExecutionContext {

    /**
     * 数据库名称。
     */
    private String databaseName;

    /**
     * 要执行的 MongoDB 命令。
     */
    private Document command;

    /**
     * 获取要执行的 MongoDB 命令。
     *
     * @return MongoDB 命令
     */
    public Document getCommand() {
        return command;
    }

    /**
     * 获取数据库名称。
     *
     * @return 数据库名称
     */
    public String getDatabaseName() {
        return databaseName;
    }

}
