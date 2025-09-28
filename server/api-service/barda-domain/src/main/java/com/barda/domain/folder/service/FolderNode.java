package com.barda.domain.folder.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;

/**
 * 一个表示文件夹结构中的节点的类。
 *
 * @param <T> 节点中包含的元素的类型。
 * @param <F> 文件夹数据类型。
 */
@Getter
@Setter
public class FolderNode<T, F> implements Node<T, F> {

    private final F self; // 节点自身的数据
    private FolderNode<T, F> parent; // 父节点
    protected final Collection<Node<T, F>> children; // 子节点集合
    @Nonnull
    private final Function<F, String> idExtractor; // 用于提取节点ID的函数
    @Nonnull
    private final Function<F, String> parentIdExtractor; // 用于提取父节点ID的函数

    /**
     * 构造函数。
     *
     * @param self 节点自身的数据
     * @param idExtractor 用于提取节点ID的函数
     * @param parentIdExtractor 用于提取父节点ID的函数
     * @param comparator 用于对子节点进行排序的比较器，可以为null
     */
    FolderNode(F self, @Nonnull Function<F, String> idExtractor, @Nonnull Function<F, String> parentIdExtractor,
            @Nullable Comparator<Node<T, F>> comparator) {
        this.self = self;
        this.idExtractor = idExtractor;
        this.parentIdExtractor = parentIdExtractor;
        this.children = comparator == null ? new ArrayList<>() : new PriorityQueue<>(comparator);
    }

    /**
     * 获取节点的ID。
     *
     * @return 节点的ID
     */
    public String id() {
        return idExtractor.apply(self);
    }

    /**
     * 获取父节点的ID。
     *
     * @return 父节点的ID
     */
    @Override
    public String parentId() {
        return parentIdExtractor.apply(self);
    }

    /**
     * 获取子节点中包含的文件夹的集合。
     *
     * @return 子节点中包含的文件夹的集合
     */
    public final List<F> getFolderChildren() {
        return children.stream()
                .filter(node -> node instanceof FolderNode<T, F>)
                .map(node -> ((FolderNode<T, F>) node).getSelf())
                .toList();
    }

    /**
     * 获取子节点中包含的元素的集合。
     *
     * @return 子节点中包含的元素的集合
     */
    public final List<T> getElementChildren() {
        return children.stream()
                .filter(node -> node instanceof ElementNode<T, F>)
                .map(node -> ((ElementNode<T, F>) node).getSelf())
                .toList();
    }

    /**
     * 获取所有子节点中包含的文件夹的集合，包括嵌套的子文件夹。
     *
     * @return 所有子节点中包含的文件夹的集合
     */
    public final List<F> getAllFolderChildren() {
        return this.children.stream()
                .map(node -> {
                    if (node instanceof FolderNode<T, F> folderNode) {
                        F self = folderNode.getSelf();
                        List<F> folderChildren = folderNode.getAllFolderChildren();
                        List<F> all = new ArrayList<>(folderChildren);
                        if (self != null) {
                            all.add(self);
                        }
                        return all;
                    }
                    return new ArrayList<F>();
                })
                .flatMap(List::stream)
                .toList();
    }

    /**
     * 后序遍历节点，先对子节点进行操作，然后对父节点进行操作。
     *
     * @param consumer 对节点进行操作的函数
     */
    public void postOrderIterate(Consumer<Node<T, F>> consumer) {
        children.forEach(node -> {
            if (node instanceof FolderNode<T, F> folderNode) {
                folderNode.postOrderIterate(consumer);
                return;
            }
            consumer.accept(node);
        });
        consumer.accept(this);
    }

    /**
     * 获取节点的深度。
     *
     * @return 节点的深度
     */
    public final int depth() {
        if (parent == null) {
            return 1;
        }
        return parent.depth() + 1;
    }

    /**
     * 重写toString方法，返回节点的字符串表示形式。
     *
     * @return 节点的字符串表示形式
     */
    @Override
    public String toString() {
        return "FolderNode{" +
                "self=" + self +
                ", children=" + children +
                '}';
    }
}