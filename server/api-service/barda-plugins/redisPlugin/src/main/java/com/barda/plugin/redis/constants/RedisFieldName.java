package com.barda.plugin.redis.constants;

/**
 * 定义了 Redis 字段名称的常量。
 */
public class RedisFieldName {

    /**
     * 组件类型。
     */
    public static final String COMMAND = "compType";

    /**
     * 组件。
     */
    public static final String COMP = "comp";

    /**
     * 原始命令。
     */
    public static final String RAW_COMMAND = COMP + "." + "command";

    /**
     * 键。
     */
    public static final String KEY = COMP + "." + "key";

    /**
     * 键列表。
     */
    public static final String KEYS = COMP + "." + "keys";

    /**
     * 值。
     */
    public static final String VALUE = COMP + "." + "value";

    /**
     * 模式。
     */
    public static final String PATTERN = COMP + "." + "pattern";

    /**
     * 字段。
     */
    public static final String FIELD = COMP + "." + "field";

    /**
     * 字段列表。
     */
    public static final String FIELDS = COMP + "." + "fields";

    /**
     * 索引。
     */
    public static final String INDEX = COMP + "." + "index";

    /**
     * 计数。
     */
    public static final String COUNT = COMP + "." + "count";

    /**
     * 源。
     */
    public static final String SOURCE = COMP + "." + "source";

    /**
     * 目的地。
     */
    public static final String DESTINATION = COMP + "." + "destination";

    /**
     * 开始。
     */
    public static final String START = COMP + "." + "start";

    /**
     * 结束。
     */
    public static final String STOP = COMP + "." + "stop";

    /**
     * 成员。
     */
    public static final String MEMBER = COMP + "." + "member";

    /**
     * 分数。
     */
    public static final String SCORE = COMP + "." + "score";

    /**
     * 最小值。
     */
    public static final String MIN = COMP + "." + "min";

    /**
     * 最大值。
     */
    public static final String MAX = COMP + "." + "max";
}
