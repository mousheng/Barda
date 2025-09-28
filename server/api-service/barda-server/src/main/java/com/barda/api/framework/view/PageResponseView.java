package com.barda.api.framework.view;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

/**
 * 包含分页信息的响应视图类。
 * 继承自{@link ResponseView}，并使用Lombok的@Getter注解来生成getter方法。
 *
 * @param <T> 响应数据中元素的类型
 */
@Getter
public class PageResponseView<T> extends ResponseView<List<T>> {

    /**
     * 当前页码
     */
    private final int pageNum;

    /**
     * 每页的大小
     */
    private final int pageSize;

    /**
     * 总记录数
     */
    private final int total;

    /**
     * 私有构造函数，用于创建PageResponseView实例。
     *
     * @param code 状态码
     * @param message 消息
     * @param data 响应数据
     * @param pageNum 当前页码
     * @param pageSize 每页的大小
     * @param total 总记录数
     */
    private PageResponseView(int code, String message, List<T> data, int pageNum, int pageSize, int total) {
        super(code, message, data);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }

    /**
     * 创建一个成功的PageResponseView实例。
     *
     * @param data 响应数据
     * @param pageNum 当前页码
     * @param pageSize 每页的大小
     * @param total 总记录数
     * @param <T> 响应数据中元素的类型
     * @return 成功的PageResponseView实例
     */
    public static <T> PageResponseView<T> success(List<T> data, int pageNum, int pageSize, int total) {
        return new PageResponseView<>(SUCCESS, "", data, pageNum, pageSize, total);
    }

    /**
     * 创建一个失败的PageResponseView实例。
     *
     * @param code 状态码
     * @param message 消息
     * @param pageNum 当前页码
     * @param pageSize 每页的大小
     * @param total 总记录数
     * @param <T> 响应数据中元素的类型
     * @return 失败的PageResponseView实例
     */
    public static <T> PageResponseView<T> error(int code, String message, int pageNum, int pageSize, int total) {
        return new PageResponseView<>(code, message, Collections.emptyList(), pageNum, pageSize, total);
    }
}
