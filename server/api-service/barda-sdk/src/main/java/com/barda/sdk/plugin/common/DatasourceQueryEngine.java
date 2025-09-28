package com.barda.sdk.plugin.common;

import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.query.QueryExecutionContext;

/**
 * DatasourceQueryEngine接口负责以下功能：
 * 1. 数据源配置解析和验证
 * 2. 连接的生命周期管理：创建/销毁/测试
 * 3. 查询上下文的构建和执行（结构可以视为特殊情况）
 *
 * @param <DatasourceConfig> 数据源配置对象的类型，必须是DatasourceConnectionConfig的子类
 * @param <Connection>       连接对象的类型
 * @param <Context>          查询执行上下文对象的类型，必须是QueryExecutionContext的子类
 */
public interface DatasourceQueryEngine<DatasourceConfig extends DatasourceConnectionConfig, Connection, Context extends QueryExecutionContext>
        extends DatasourceConnector<Connection, DatasourceConfig>, QueryExecutor<DatasourceConfig, Connection, Context> {

}
