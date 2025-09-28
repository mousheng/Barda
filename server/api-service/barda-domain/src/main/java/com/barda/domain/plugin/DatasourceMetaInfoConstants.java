package com.barda.domain.plugin;

/**
 * 该类包含了一些常量，用于表示数据源的类型。
 */
public final class DatasourceMetaInfoConstants {

    /**
     * 常量，表示 REST API 数据源。
     */
    public static final String REST_API = "restApi";

    /**
     * 常量，表示 GraphQL API 数据源。
     */
    public static final String GRAPHQL_API = "graphql";

    /**
     * 常量，表示 MySQL 数据库数据源。
     */
    public static final String MYSQL = "mysql";

    /**
     * 常量，表示 Barda API 数据源。
     */
    public static final String BARDA_API = "bardaApi";

    /**
     * 私有构造函数，防止该类被实例化。
     */
    private DatasourceMetaInfoConstants() {
    }
}
