![](../assets/1-20231002173002-b5hpgja.png)​

[OceanBase](https://www.oceanbase.com/) 是蚂蚁集团自主研发的高性能分布式关系型数据库系统。通过自主研发的分布式事务引擎、高性能存储引擎和智能优化器等技术，实现了多副本数据自动同步和故障恢复、高效数据查询和修改、以及数据安全保护等功能。此外，它还提供了丰富的数据管理工具和 API，支持 SQL 语言和 NoSQL 接口，能满足各种应用场景的需求。

目前Barda已经实现了与 OceanBase 数据源的连接，支持对 OceanBase 数据进行增、删、改、查， 同时还支持将数据绑定至各种组件，并通过简单的代码实现数据的可视化和计算等操作，能让您快速、高效地搭建应用和内部系统。

## 准备

正式开始前，您需要获取 OceanBase 数据库的连接配置，并参考[IP 白名单](../ip-allowlist.md)文档将Barda的 IP 地址添加到数据库网络的**白名单**中（按需配置）。

## 新建数据源

在[Barda主页](https://barda.mousheng.top/apps)左下角，点击**数据源**进入当前企业的数据源管理界面，然后点击右上角 **+ 新建数据源** > ​**OceanBase**​，填写您的 OceanBase 数据库的配置信息。点击​**测试连接**​，提示**连接成功**后再点击**保存**按钮，该 OceanBase 数据源即新建完成，并且保存至企业的数据源列表中。

![](../assets/2-20231002173002-5q8ctfs.png)​

## 创建查询

在应用编辑页面，点击**新建**创建查询，选择您的 OceanBase 数据源，然后编写 SQL 查询语句。Barda中支持 **SQL 模式**和 **GUI模式**​，让您能够更加灵活便捷地操作数据。关于在Barda中使用 OceanBase SQL 的详细教程，可参阅文档[SQL 基础操作（MySQL 模式）](https://www.oceanbase.com/docs/common-oceanbase-database-cn-10000000001577908)。

![](../assets/3-20231002173002-uq7ylel.png)​

编写完成后，点击**运行**可查看查询的执行结果。如果将运行结果与Barda中[组件](../component-guides/README.md)的数据字段绑定，就能使数据可视化。

![](../assets/4-20231002173002-6lkk950.png)​