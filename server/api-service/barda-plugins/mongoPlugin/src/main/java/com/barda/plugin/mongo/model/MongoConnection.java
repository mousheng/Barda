package com.barda.plugin.mongo.model;

import org.bson.Document;

import com.google.common.base.Preconditions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;

import reactor.core.publisher.Mono;

/**
 * 用于管理 MongoDB 连接的类。
 */
public class MongoConnection {

    /**
     * 用于测试 MongoDB 连接的查询命令。
     */
    private static final String TEST_CONNECTION_QUERY = "ping";

    /**
     * 用于 MongoDB 连接的 MongoClient 对象。
     */
    private final MongoClient mongoClient;

    /**
     * 要连接的 MongoDB 数据库名称。
     */
    private final String database;

    /**
     * 私有构造器，使用 MongoClient 和数据库名称创建 MongoConnection 对象。
     *
     * @param mongoClient 用于 MongoDB 连接的 MongoClient 对象
     * @param database 要连接的 MongoDB 数据库名称
     */
    public MongoConnection(MongoClient mongoClient, String database) {
        Preconditions.checkNotNull(mongoClient);
        this.mongoClient = mongoClient;
        this.database = database;
    }

    /**
     * 获取 MongoDatabase 对象，用于执行 MongoDB 数据库操作。
     *
     * @return MongoDatabase 对象
     */
    public MongoDatabase getDatabase() {
        return mongoClient.getDatabase(database);
    }

    /**
     * 执行 ping 命令，测试 MongoDB 连接是否正常。
     *
     * @return 包含 ping 命令结果的 Mono<Document> 对象
     */
    public Mono<Document> ping() {
        return Mono.from(mongoClient.getDatabase(database).runCommand(new Document(TEST_CONNECTION_QUERY, 1)));
    }

    /**
     * 关闭 MongoDB 连接。
     *
     * @return 空的 Mono<Void> 对象，表示关闭操作已完成
     */
    public Mono<Void> close() {
        return Mono.fromRunnable(mongoClient::close);
    }
}