## Barda

**barda**继承自废弃的**openblocks**项目，**openblocks** 则是国内低代码平台**码匠**的开源版，后**码匠**项目下马。后来一个国外公司以openblocks为基础，改名为barda。因改为国外团队维护，导致没有了对国内的数据源支持，甚至中文环境一度都无法运行。作为一款国产低代码平台，居然沦落到无法在中文环境运行，实在令人唏嘘。
本人试用过appsmith、retool、tooljet、码匠，很早对码匠就挺有兴趣的，跟他们的销售人员聊的时候问及以后没有开源版来二次开发，但一直跟我说没有开源版。后来选择改造 [tooljet](https://github.com/mousheng/tooljet_cn.git),前段时间无意间发现barda原来就是码匠的开源版，于是开始对其进行改造。

Barda（以下简称barda）是[barda](https://github.com/barda-org/barda.git)的国内分支（因为barda合并代码速度太慢以及有些理念不同，特开此分支），是一款开发者友好的低代码平台。通过开箱即用的组件库、所见即所得 UI 布局以及连接数据库/API，您可以快速开发内部应用，同时无需关注复杂繁琐的前后端交互、应用的安装与部署，让您专注于业务发展。

?> 维护该项目纯粹出于对低代码平台的兴趣，因此，如果有志同道合的小伙伴，欢迎大家一起交流、讨论、贡献代码,如果你是码匠的前开发人员，更希望你能联系我，因为真的有很多问题需要请教🥺

![](assets/what-is-barda-20231002133803-7j4cpkm.gif)​

## 快速开始

### 在线试用

请访问 https://barda.mousheng.top

!> 请不要将自己的api令牌或者账号密码保存在共享账号中

> #### 账号密码
> 账户： `test@mousheng.top`
> 
> 密码： `test123456`

### 私有化部署
请参阅 [docker部署](docker.md)

## 搭建应用的步骤？

只需通过几个步骤，便可搭建和使用您的内部应用：

1. 通过 GUI 快速[连接数据源](datasource.md)。
2. 编写少量代码[构建查询](how-to-write-query.md)。
3. 使用开箱即用的组件库轻松[搭建应用界面](drag-and-drop.md)，使用 [JavaScript 表达式](javascript-in-barda/writing-javascript.md)绑定查询数据。
4. 触发查询/控制组件/响应用户行为/...，通过设置[事件触发](event-handler.md)提供响应式 UI 交互。
5. [预览](app-release.md)并将您的应用分享给其他人。

* [教程：如何搭建一个应用？](quick-tutorial.md)
