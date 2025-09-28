![](../assets/5-20231002173006-ydk1p3w.png)​

[openGauss](https://www.opengauss.org/zh/) 是由华为公司开发的一款开源关系型数据库管理系统，它基于 PostgreSQL 数据库引擎，针对大规模数据处理、分布式、高可用性等场景进行了优化，支持复杂查询和高并发访问，能够满足大规模企业级应用的需求。同时，openGauss 还提供了丰富的数据安全性功能，包括数据加密、访问控制、审计等，保障数据的安全性和可靠性。

目前Barda已经实现了与 openGauss 数据源的连接，支持对 openGauss 数据进行增、删、改、查， 同时还支持将数据绑定至各种组件，并通过简单的代码实现数据的可视化和计算等操作，能让您快速、高效地搭建应用和内部系统。

## 准备

正式开始前，您需要获取 openGauss 数据库的连接配置，并参考[IP 白名单](../ip-allowlist.md)文档将Barda的 IP 地址添加到数据库网络的**白名单**中（按需配置）。

## 新建数据源

在[Barda主页](https://barda.mousheng.top/apps)左下角，点击**数据源**进入当前企业的数据源管理界面，然后点击右上角 **+ 新建数据源** > ​**openGauss**​，填写您的 openGauss 数据库的配置信息。点击​**测试连接**​，提示**连接成功**后再点击**保存**按钮，该 openGauss 数据源即新建完成，并且保存至企业的数据源列表中。

![](../assets/6-20231002173006-yjb3c4b.png)​

## 创建查询

在应用编辑页面，点击**新建**创建查询，选择您的 openGauss 数据源，然后编写 SQL 查询语句。Barda中支持 **SQL 模式**和 **GUI模式**​，让您能够更加灵活便捷地操作数据。关于在Barda中使用 openGauss SQL 的详细教程，可参阅[附录：SQL语法](https://docs.opengauss.org/zh/docs/3.1.1/docs/BriefTutorial/%E9%99%84%E5%BD%95-SQL%E8%AF%AD%E6%B3%95.html)。

![](../assets/7-20231002173006-vmkf1we.png)​

编写完成后，点击**运行**可查看查询的执行结果。如果将运行结果与Barda中[组件](../component-guides/README.md)的数据字段绑定，就能使数据可视化。

![](../assets/8-20231002173006-cck2y3s.png)​