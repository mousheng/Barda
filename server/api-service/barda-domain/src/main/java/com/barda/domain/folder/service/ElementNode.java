package com.barda.domain.folder.service;

import java.util.function.Function;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;

/**
 * 实现节点的类。
 * 该类表示具有父文件夹的元素节点。
 *
 * @param <T> 节点中包含的数据的类型
 * @param <F> 父文件夹中包含的数据的类型
 */
@Setter
@Getter
public class ElementNode<T, F> implements Node<T, F> {

    /**
     * 节点中包含的数据。
     */
    @Nonnull
    private final T self;

    /**
     * 父文件夹。
     */
    @Nonnull
    private FolderNode<T, F> parent;

    /**
     * 获取父文件夹 ID 的函数。
     */
    @Nonnull
    private final Function<T, String> parentIdExtractor;

    /**
     * 构造函数。
     *
     * @param self 节点中包含的数据
     * @param parentIdExtractor 获取父文件夹 ID 的函数
     */
    ElementNode(@Nonnull T self, @Nonnull Function<T, String> parentIdExtractor) {
        this.self = self;
        this.parentIdExtractor = parentIdExtractor;
    }

    /**
     * 获取父文件夹的 ID。
     *
     * @return 父文件夹的 ID
     */
    @Override
    public String parentId() {
        return parentIdExtractor.apply(self);
    }

    /**
     * 重写 toString() 方法。
     *
     * @return 节点的字符串表示形式
     */
    @Override
    public String toString() {
        return "ElementNode{" +
                "self=" + self +
                '}';
    }
}