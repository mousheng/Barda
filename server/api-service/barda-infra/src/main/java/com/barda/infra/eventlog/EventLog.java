package com.barda.infra.eventlog;

import java.util.Map;

/**
 * 一个表示事件日志的记录类。
 * 该类使用 Java 14 的记录（Record）功能来表示一个不可变的数据类。
 */
public record EventLog(String deploymentId, String deploymentVersion, String eventType, Map<String, Object> log) {

}
