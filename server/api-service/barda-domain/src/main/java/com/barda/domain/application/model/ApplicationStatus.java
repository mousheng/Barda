package com.barda.domain.application.model;

/**
 * 该枚举表示应用程序在系统中的状态。
 *
 * <p>枚举包括以下状态：
 * <ul>
 *     <li>{@link #NORMAL}：新应用程序的默认状态。</li>
 *     <li>{@link #RECYCLED}：表示应用程序已被回收。</li>
 *     <li>{@link #DELETED}：表示应用程序已被删除。</li>
 * </ul>
 *
 * <p>
 * 请注意，此处的注释是根据中文翻译的，您可以根据您的需求进行修改。
 */
public enum ApplicationStatus {

    NORMAL, // default
    RECYCLED,
    DELETED,

}
