package com.barda.domain.folder.service;

/**
 * 定义节点的接口。
 * 该接口用于表示具有父文件夹的节点。
 *
 * @param <T> 节点中包含的数据的类型
 * @param <F> 父文件夹中包含的数据的类型
 */
public interface Node<T, F> {

    /**
     * 获取父文件夹的 ID。
     *
     * @return 父文件夹的 ID
     */
    String parentId();

    /**
     * 设置父文件夹。
     *
     * @param folderNode 父文件夹
     */
    void setParent(FolderNode<T, F> folderNode);
}