package com.barda.domain.folder.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 一个表示树结构的类，继承自FolderNode。
 *
 * @param <T> 元素类型。
 * @param <F> 文件夹类型。
 */
@Slf4j
public class Tree<T, F> extends FolderNode<T, F> {
    private static final int DEFAULT_DEPTH = 1;
    private final int maxDepth;
    private final Map<String, FolderNode<T, F>> folderId2FolderNodeMap;

    /**
     * 用于提取文件夹节点ID的函数。
     */
    public final Function<F, String> folderNodeIdExtractor;

    /**
     * 用于提取文件夹节点父节点ID的函数。
     */
    public final Function<F, String> folderNodeParentIdExtractor;

    /**
     * 用于提取元素节点父节点ID的函数。
     */
    public final Function<T, String> elementNodeParentIdExtractor;

    /**
     * 构造函数。
     *
     * @param folders 初始文件夹列表。
     * @param folderNodeIdExtractor 用于提取文件夹节点ID的函数。
     * @param folderNodeParentIdExtractor 用于提取文件夹节点父节点ID的函数。
     * @param elements 初始元素列表。
     * @param elementNodeParentIdExtractor 用于提取元素节点父节点ID的函数。
     * @param comparator 用于对节点进行排序的比较器，可以为null。
     */
    public Tree(List<F> folders,
            Function<F, String> folderNodeIdExtractor,
            Function<F, String> folderNodeParentIdExtractor,
            List<T> elements,
            Function<T, String> elementNodeParentIdExtractor,
            @Nullable Comparator<Node<T, F>> comparator) {
        this(folders, folderNodeIdExtractor, folderNodeParentIdExtractor, elements, elementNodeParentIdExtractor, DEFAULT_DEPTH, comparator);
    }

    /**
     * 构造函数。
     *
     * @param folders 初始文件夹列表。
     * @param folderNodeIdExtractor 用于提取文件夹节点ID的函数。
     * @param folderNodeParentIdExtractor 用于提取文件夹节点父节点ID的函数。
     * @param elements 初始元素列表。
     * @param elementNodeParentIdExtractor 用于提取元素节点父节点ID的函数。
     * @param maxDepth 树的最大深度。
     * @param comparator 用于对节点进行排序的比较器，可以为null。
     */
    public Tree(List<F> folders,
            Function<F, String> folderNodeIdExtractor,
            Function<F, String> folderNodeParentIdExtractor,
            List<T> elements,
            Function<T, String> elementNodeParentIdExtractor,
            int maxDepth,
            @Nullable Comparator<Node<T, F>> comparator) {
        super(null, folderNodeIdExtractor, folderNodeParentIdExtractor, comparator);
        this.folderNodeIdExtractor = folderNodeIdExtractor;
        this.folderNodeParentIdExtractor = folderNodeParentIdExtractor;
        this.elementNodeParentIdExtractor = elementNodeParentIdExtractor;
        this.maxDepth = maxDepth;

        this.folderId2FolderNodeMap = folders.stream()
                .map(folder -> new FolderNode<>(folder, folderNodeIdExtractor, folderNodeParentIdExtractor, comparator))
                .collect(Collectors.toMap(FolderNode::id, Function.identity()));
        mount(this.folderId2FolderNodeMap.values());
        mount(elements.stream().map(element -> new ElementNode<T, F>(element, elementNodeParentIdExtractor)).toList());
    }

    /**
     * 挂载节点并构建树结构。
     *
     * @param nodes 要挂载的节点列表。
     */
    private void mount(Collection<? extends Node<T, F>> nodes) {
        nodes.forEach(node -> {
            if (StringUtils.isBlank(node.parentId())) {
                children.add(node);
                node.setParent(this);
                return;
            }
            FolderNode<T, F> parent = folderId2FolderNodeMap.get(node.parentId());
            if (parent == null) {
                log.warn("error node: {}", node);
                // 父节点未找到，仍将其添加到树中
                children.add(node);
                node.setParent(this);
                return;
            }
            parent.getChildren().add(node);
            node.setParent(parent);
        });
    }

    /**
     * 获取指定ID的节点。
     *
     * @param folderId 要获取的节点的ID。
     * @return 节点。
     */
    public FolderNode<T, F> get(@Nullable String folderId) {
        if (StringUtils.isBlank(folderId)) {
            return this;
        }
        return folderId2FolderNodeMap.get(folderId);
    }

    /**
     * 重写id方法，不支持此操作。
     *
     * @return 空字符串。
     * @throws UnsupportedOperationException 总是抛出此异常。
     */
    @Override
    public String id() {
        throw new UnsupportedOperationException();
    }

    /**
     * 重写parentId方法，不支持此操作。
     *
     * @return 空字符串。
     * @throws UnsupportedOperationException 总是抛出此异常。
     */
    @Override
    public String parentId() {
        throw new UnsupportedOperationException();
    }

    /**
     * 重写toString方法，返回树的字符串表示形式。
     *
     * @return 树的字符串表示形式。
     */
    @Override
    public String toString() {
        return "Tree{" +
                "maxDepth=" + maxDepth +
                ", children=" + children +
                '}';
    }
}
