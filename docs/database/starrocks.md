![](../assets/1-20231002173025-2ea0a6v.png)​

[StarRocks](https://www.starrocks.io/) 是开源的新一代极速全场景 MPP（Massively Parallel Processing，即大规模并行处理）数据库。它采用新一代的弹性 MPP 架构，可以高效支持大数据量级的多维分析、实时分析、高并发分析等多种数据分析场景。StarRocks 性能出色，它采用了全面向量化技术，比同类产品平均快 3-5 倍。

目前Barda已经实现了与 StarRocks 数据源的连接，支持对 StarRocks 数据进行增、删、改、查， 同时还支持将数据绑定至各种组件，并通过简单的代码实现数据的可视化和计算等操作，能让您快速、高效地搭建应用和内部系统。

## 准备

正式开始前，您需要获取 StarRocks 数据库的连接配置，并参考[IP 白名单](../ip-allowlist.md)文档将Barda的 IP 地址添加到数据库网络的**白名单**中（按需配置）。

## 新建数据源

在[Barda主页](https://barda.mousheng.top/apps)左下角，点击**数据源**进入当前企业的数据源管理界面，然后点击右上角 **+ 新建数据源** > ​**StarRocks**​，填写您的 StarRocks 数据库的配置信息。点击​**测试连接**​，提示**连接成功**后再点击**保存**按钮，该 StarRocks 数据源即新建完成，并且保存至企业的数据源列表中。

![](../assets/2-20231002173025-16glndw.png)​

## 创建查询

在应用编辑页面，点击**新建**创建查询，选择您的 StarRocks 数据源，然后编写 SQL 查询语句。Barda中支持 **SQL 模式**和 **GUI模式**​，让您能够更加灵活便捷地操作数据。关于在Barda中使用 SQL 的详细教程，可参阅文档[使用 SQL](../using-sql.md)。

![](../assets/3-20231002173025-qts5wlt.png)​

编写完成后，点击**运行**可查看查询的执行结果。如果将运行结果与Barda中[组件](../component-guides/README.md)的数据字段绑定，就能使数据可视化。

![](../assets/4-20231002173025-vqf366v.png)​