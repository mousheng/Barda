package com.barda.plugin.mongo.constants;

/**
 * 该类包含了 MongoDB 字段名称的常量定义。
 * 这些常量在 MongoDB 聚合操作、查询、更新等过程中使用。
 */
public class MongoFieldName {

    /**
     * 用于指定命令类型。
     */
    public static final String COMMAND_TYPE = "compType";

    /**
     * 字段名称的前缀。
     */
    public static final String FIELD_PREFIX = "comp";

    /**
     * 集合名称。
     */
    public static final String COLLECTION = "collection";

    /**
     * 查询条件。
     */
    public static final String QUERY = "query";

    /**
     * 排序条件。
     */
    public static final String SORT = "sort";

    /**
     * 投影条件。
     */
    public static final String PROJECTION = "projection";

    /**
     * 限制返回的文档数量。
     */
    public static final String LIMIT = "limit";

    /**
     * 跳过的文档数量。
     */
    public static final String SKIP = "skip";

    /**
     * 更新操作。
     */
    public static final String UPDATE = "update";

    /**
     * 用于 distinct 操作的键。
     */
    public static final String KEY = "key";

    /**
     * 管道数组。
     */
    public static final String PIPELINES = "arrayPipelines";

    /**
     * 文档数组。
     */
    public static final String DOCUMENTS = "documents";

    /**
     * 原始命令。
     */
    public static final String RAW_COMMAND = FIELD_PREFIX + "." + "command";

    /**
     * 聚合管道。
     */
    public static final String AGGREGATE_PIPELINE = FIELD_PREFIX + "." + PIPELINES;

    /**
     * 聚合操作的限制。
     */
    public static final String AGGREGATE_LIMIT = FIELD_PREFIX + "." + "limit";

    /**
     * 用于 count 操作的查询条件。
     */
    public static final String COUNT_QUERY = FIELD_PREFIX + "." + QUERY;

    /**
     * 用于 delete 操作的查询条件。
     */
    public static final String DELETE_QUERY = FIELD_PREFIX + "." + QUERY;

    /**
     * 用于 delete 操作的限制。
     */
    public static final String DELETE_LIMIT = FIELD_PREFIX + "." + LIMIT;

    /**
     * 用于 distinct 操作的查询条件。
     */
    public static final String DISTINCT_QUERY = FIELD_PREFIX + "." + QUERY;

    /**
     * 用于 find 操作的查询条件。
     */
    public static final String FIND_QUERY = FIELD_PREFIX + "." + QUERY;

    /**
     * 用于 find 操作的排序条件。
     */
    public static final String FIND_SORT = FIELD_PREFIX + "." + SORT;

    /**
     * 用于 find 操作的投影条件。
     */
    public static final String FIND_PROJECTION = FIELD_PREFIX + "." + PROJECTION;

    /**
     * 用于 insert 操作的文档数组。
     */
    public static final String INSERT_DOCUMENT = FIELD_PREFIX + "." + DOCUMENTS;

    /**
     * 用于 update 操作的查询条件。
     */
    public static final String UPDATE_QUERY = FIELD_PREFIX + "." + QUERY;

    /**
     * 用于 update 操作的更新操作。
     */
    public static final String UPDATE_OPERATION = FIELD_PREFIX + "." + UPDATE;

    /**
     * 用于 distinct 操作的键。
     */
    public static final String DISTINCT_KEY = FIELD_PREFIX + "." + KEY;

    /**
     * 用于 find 操作的限制。
     */
    public static final String FIND_LIMIT = FIELD_PREFIX + "." + LIMIT;

    /**
     * 用于 find 操作的跳过。
     */
    public static final String FIND_SKIP = FIELD_PREFIX + "." + SKIP;

    /**
     * 用于 update 操作的限制。
     */
    public static final String UPDATE_LIMIT = FIELD_PREFIX + "." + LIMIT;

}
