package com.barda.plugin.mongo.model;

/**
 * 定义了 MongoDB 认证机制的枚举类。
 */
public enum MongoAuthMechanism {

    /**
     * SCRAM-SHA-1 认证机制。
     */
    SCRAM_SHA_1("SCRAM-SHA-1"),

    /**
     * SCRAM-SHA-256 认证机制。
     */
    SCRAM_SHA_256("SCRAM-SHA-256"),

    /**
     * MongoDB-X509 认证机制。
     */
    MONGODB_CR("MONGODB-X509");

    /**
     * 枚举值的字符串表示。
     */
    private final String value;

    /**
     * 私有构造器，用于创建 MongoAuthMechanism 枚举值。
     *
     * @param value 枚举值的字符串表示
     */
    MongoAuthMechanism(String value) {
        this.value = value;
    }

    /**
     * 获取枚举值的字符串表示。
     *
     * @return 枚举值的字符串表示
     */
    public String getValue() {
        return value;
    }

}