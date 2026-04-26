# JS 库管理机制

## 概述

Barda 支持两种 JS 库：**在线库**（从 CDN 加载）和 **内部库**（上传到服务器）。库在应用启动预加载阶段通过沙箱执行引擎注入到运行环境。

## 库的分类

| 类型 | 来源 | 持久化 | 加载方式 | 管理范围 |
|------|------|--------|----------|----------|
| **在线库** | CDN URL（unpkg, cdnjs 等） | 不持久化 | fetch URL → evalFunc 执行 | 组织级 / 应用级 |
| **内部共享库** | 上传 .js/.css 文件 | GridFS + 文件系统 | GET `/api/libraries/shared/{filename}` → evalFunc | 全局（主组织管理员） |
| **内部组织库** | 上传 .js/.css 文件 | GridFS + 文件系统 | GET `/api/libraries/org/{filename}` → evalFunc | 组织内（组织管理员） |

## 内部库上传流程

```
用户点击"上传" → UnifiedLibraryModal
  │
  1. 文件选择前校验
  │  ├─ 扩展名：仅 .js 或 .css
  │  └─ 大小：最大 10MB
  │
  2. 元数据表单
  │  ├─ displayName（必填，自动从文件名提取）
  │  ├─ version（必填）
  │  └─ description（可选）
  │  → FileReader 将文件转为 base64
  │
  3. API 调用
  │  POST /api/libraries/{shared|org}/upload
  │  Body: { filename, content(base64), displayName, version, description }
  │
  4. 后端处理 (LibraryApiService)
  │  ├─ 权限校验：shared=主组织管理员, org=组织开发员
  │  ├─ 格式校验：文件名非空、扩展名 .js/.css
  │  ├─ 内容校验：base64 解码、检查大小 ≤ 10MB
  │  ├─ 生成 libraryId：displayName 小写去特殊字符，空格转连字符
  │  ├─ 生成存储文件名："{libraryId}@{version}.{ext}"
  │  ├─ 去重检查：findByFilenameAndType
  │  ├─ 配额检查：shared ≤ 100MB, org ≤ 50MB（总大小）
  │  ├─ 构建 LibraryMeta 实体
  │  ├─ GridFS 存储：libraryStorageService.saveToGridFs()
  │  ├─ 文件系统同步：libraryStorageService.syncToFileSystem()
  │  └─ 元数据保存：libraryMetaRepository.save()
  │
  5. 返回 LibraryView → UI 刷新列表
```

## 内部库删除流程

```
用户点击删除 → Popconfirm 确认
  │
  API: DELETE /api/libraries/{shared|org}/{filename}
  │
  后端处理 (LibraryApiService)
  ├─ 权限校验：shared=主组织管理员, org=组织管理员
  ├─ 查找 LibraryMeta：by filename + type
  ├─ 删除 GridFS：libraryStorageService.deleteFromGridFs()
  ├─ 删除文件系统：libraryStorageService.deleteFromFileSystem()
  ├─ 删除元数据：libraryMetaRepository.delete()
  └─ 清理预加载引用：removeLibraryFromPreloadLists()
     ├─ SHARED 类型：遍历所有组织，从 preloadLibs 移除
     └─ ORG 类型：仅从当前组织的 preloadLibs 移除
```

## 运行时注入

### 应用级加载（编辑器设置侧边栏）

```
用户点击 "+" → UnifiedLibraryModal
  ├─ onCheck(url): 检查 URL 是否已在库列表
  ├─ onLoad(url): 调用 LibsComp.loadScript(url) 即时验证
  ├─ onSuccess(url): 派发 pushAction 将 URL 加入库列表
  └─ onDelete(url): 派发 deleteAction 从库列表移除
→ URL 存储在应用 DSL 中
→ 应用启动时由 PreloadComp.run() 加载
```

### 组织级加载（管理后台设置）

```
用户打开高级设置 → UnifiedLibraryModal
  ├─ onCheck(url): 检查 URL 是否已在 orgCommonSettings.preloadLibs
  ├─ onLoad(url): fetchJSLibrary(url) → evalFunc(code, {}) 即时验证
  ├─ onSuccess(url): 调用 handleSave("preloadLibs") 持久化
  └─ onDelete(url): 调用 handleSave("preloadLibs") 移除
→ URL 存储在 orgCommonSettings.preloadLibs
→ 应用启动时作为 externalLibs 参数传入 PreloadComp.run()
```

## 在线库元数据获取

```
GET /api/misc/js-library/metas?name=jquery,lodash
  │
  JsLibraryController.getMeta()
  │
  对每个名称：
  ├─ 是内部路径（/api/libraries/shared/...）？→ LibraryApiService 查询
  ├─ 是推荐库？→ 返回缓存 RECOMMENDED_JS_LIB_META_CACHE
  └─ 否则 → WebClient 请求 https://registry.npmjs.com/{name}
       ├─ ExchangeStrategies 无大小限制
       ├─ LoadingCache 缓存 1 天，最大 10000 条
       └─ 解析：description, homepage（repository URL 兜底）, latestVersion

GET /api/misc/js-library/recommendations
  → 返回 recommendedJsLibraries.json 中所有推荐的库元数据
```

## 后端 API 端点

| 端点 | 方法 | 用途 |
|------|------|------|
| `/api/libraries/shared/upload` | POST | 上传共享库 |
| `/api/libraries/org/upload` | POST | 上传组织库 |
| `/api/libraries/shared/{filename}` | DELETE | 删除共享库 |
| `/api/libraries/org/{filename}` | DELETE | 删除组织库 |
| `/api/libraries/shared/{filename}` | GET | 下载共享库（用于注入执行） |
| `/api/libraries/org/{filename}` | GET | 下载组织库 |
| `/api/libraries/shared/list/{orgId}` | GET | 列出共享库 |
| `/api/libraries/org/list/{orgId}` | GET | 列出组织库 |
| `/api/libraries/shared/available/{orgId}` | GET | 列出该组织可用的共享库 |
| `/api/misc/js-library/metas` | GET | 获取在线库元数据 |
| `/api/misc/js-library/recommendations` | GET | 获取推荐库列表 |

## 存储架构

```
Upload Flow:
  用户文件 → base64 → API → LibraryApiService
                              ├─ GridFS (MongoDB) — 主要持久化存储
                              └─ 文件系统 /barda/client/static/lib/ — 可选同步

Download Flow:
  客户端 fetch(/api/libraries/.../filename)
    → LibraryController
      → LibraryStorageService.downloadFromGridFs() — 从 GridFS 流式读取
    → 响应头: Content-Disposition: inline
              Content-Type: text/javascript 或 text/css
              Cache-Control: max-age=7d
    → 客户端 evalFunc(code) 或 evalStyle(css) 执行

Startup Restore:
  LibraryFileSyncRunner (每次启动)
    → 遍历 GridFS 中的库文件
    → 恢复到文件系统
```

## 启动预填充

`PreloadLibraryRunner` 在首次启动时读取 `preload-libraries.json`：

| 库名 | 用途 |
|------|------|
| jmespath | JSON 查询表达式 |
| jspdf | PDF 生成 |
| i18next | 国际化 |
| jsonpath | JSON 路径查询 |

检查共享库是否为空（首次运行），从 `/app/preload-libraries/` 读取文件，创建 LibraryMeta 实体，保存到 GridFS 和文件系统。

## 关键文件

### 前端

| 文件路径 | 职责 |
|----------|------|
| `client/packages/barda/src/components/UnifiedLibraryModal.tsx` | 主 UI 组件 — 三标签页模态框（在线/共享/组织） |
| `client/packages/barda/src/components/JSLibraryTree.tsx` | 库列表展示组件 |
| `client/packages/barda/src/api/libraryApi.ts` | 内部库 CRUD API 客户端 |
| `client/packages/barda/src/api/jsLibraryApi.ts` | 在线库元数据 API 客户端 |
| `client/packages/barda/src/util/jsLibraryUtils.ts` | URL 解析和 fetch 工具函数 |
| `client/packages/barda/src/comps/comps/preLoadComp.tsx` | LibsComp — 运行时库加载的核心 |
| `client/packages/barda/src/pages/setting/advanced/AdvancedSetting.tsx` | 组织级设置页面 |

### 后端

| 文件路径 | 职责 |
|----------|------|
| `barda-server/.../library/LibraryController.java` | REST 控制器（上传/删除/列表/下载） |
| `barda-server/.../library/LibraryApiService.java` | 业务逻辑（校验、存储、权限） |
| `barda-server/.../misc/JsLibraryController.java` | 在线库元数据 API（npm 查询） |
| `barda-domain/.../library/model/LibraryMeta.java` | MongoDB 文档实体 |
| `barda-domain/.../library/model/LibraryType.java` | SHARED / ORG 枚举 |
| `barda-domain/.../library/repository/LibraryMetaRepository.java` | MongoDB 数据访问 |
| `barda-domain/.../library/service/LibraryStorageService.java` | GridFS + 文件系统存储 |
| `barda-server/.../runner/PreloadLibraryRunner.java` | 首次启动库预填充 |
| `barda-server/.../runner/LibraryFileSyncRunner.java` | 启动时 GridFS → 文件系统恢复 |
| `barda-sdk/.../config/LibraryConfig.java` | 配置（大小限制、路径） |

### 配置文件

| 文件路径 | 职责 |
|----------|------|
| `barda-server/src/main/resources/recommendedJsLibraries.json` | 4 个推荐在线库 |
| `barda-server/src/main/resources/preload-libraries.json` | 4 个预装库 |
