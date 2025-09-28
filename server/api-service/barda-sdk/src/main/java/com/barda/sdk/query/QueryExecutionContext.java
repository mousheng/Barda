package com.barda.sdk.query;

/**
 * 查询执行上下文的抽象类。
 * 该类为查询执行提供了一个通用的框架，并定义了访问者 ID 相关的操作。
 */
public abstract class QueryExecutionContext {

    /**
     * 访问者 ID。
     */
    private String visitorId;

    /**
     * 获取访问者 ID。
     *
     * @return 访问者 ID。
     */
    public String getVisitorId() {
        return visitorId;
    }

    /**
     * 设置访问者 ID。
     *
     * @param visitorId  要设置的访问者 ID。
     */
    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

}
