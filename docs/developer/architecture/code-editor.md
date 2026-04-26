# 代码编辑器 (CodeMirror) 机制

## 概述

Barda 基于 CodeMirror 6 构建了代码编辑器系统，支持多种语言模式。核心能力在于 JavaScript 编辑模式下自动补全组件名、属性方法、查询名、返回值等应用上下文信息。

## 编辑器种类

### 语言模式

```typescript
type Language = "sql" | "javascript" | "css" | "html" | "json";
```

各语言映射到 CodeMirror 语言扩展：
- `javascript` → `@codemirror/lang-javascript`
- `sql` → `@codemirror/lang-sql`
- `css` → `@codemirror/lang-css`
- `html` → `@codemirror/lang-html`
- `json` → `@codemirror/lang-json`

### 代码类型 (CodeType)

```typescript
type CodeType = undefined | "JSON" | "Function" | "PureJSON";
```

| CodeType | 行为 |
|----------|------|
| `undefined`（默认） | 混合模式，`{{...}}` 中是 JS 表达式，其余为静态文本。高亮仅作用于 JS 段 |
| `"Function"` | 全函数体模式。激活 ESLint 校验，`checkCursorInBinding()` 始终返回 true |
| `"JSON"` | JSON 模式，宽松解析，添加 `jsonParseLinter()` |
| `"PureJSON"` | 严格 JSON 模式 |

## JavaScript 自动补全系统

补全系统由 **三个独立的 CompletionSource** 组合而成，每个负责不同的补全维度。

### 补全架构总览

```
CodeEditor 组件
  │
  └─ useCompletionSources()          [extensions.tsx]
       │
       ├─ ExposingCompletionSource   ── 应用上下文实体（组件、查询、状态等）
       │
       ├─ TernServer                 ── JS 语法推理（内置 API、lodash、类型推断）
       │
       └─ SQLCompletionSource        ── SQL 关键字 + 表元数据（仅 SQL 模式）
       │
       └─ → autocompletion({ override: completions })
```

## 补全源详解

### 1. ExposingCompletionSource — 应用上下文补全

**文件**: `base/codeEditor/completion/exposingCompletionSource.tsx`

**职责**: 补全应用中所有的命名实体：组件名、查询名、临时状态、转换器、数据响应器及其属性。

#### 匹配机制

每次按键时，通过正则提取用户正在输入的路径：

```typescript
context.matchBefore(/(?:[A-Za-z_$][\w$]*(?:\[\s*(?:\d+|(["'])(?:[^\1\\]|\\.)*?\1)\s*\])*\.)*(?:[A-Za-z_$][\w$]*)?/)
```

然后调用 `getDataInfo(data, path)` 对路径前缀求值：

```
输入: "table1.sel"
  → getDataInfo(exposingData, "table1.sel")
  → evalScript("table1", data) 获取 table1 对象
  → 返回 table1 的所有键（selectedRow, text 等）
  → 过滤以 "sel" 开头的: "selectedRow"
```

#### 优先级提升

常用属性在子属性匹配时获得 `boost: 10`，排在补全列表最前面：

| 优先级属性 | 优先级方法 |
|-----------|-----------|
| `value`, `selectedRow`, `data`, `text` | `setValue`, `setData` |

### 2. TernServer — JavaScript 语法补全

**文件**: `base/codeEditor/completion/ternServer.tsx`

**职责**: 利用 Tern.js 引擎进行 JavaScript 类型推断补全，包括 ECMAScript 内置 API、Lodash 方法。

#### 关键机制：生成上下文代码

TernServer 将应用中的 exposing 数据序列化为**带 JSDoc 类型注解的虚拟 JS 代码**，注入到 Tern.js 引擎中：

```javascript
// TernServer.generateContextCode() 生成的示例
/** @type {{selectedRow: Array, data: string, text: string, ...}} */
var table1 = { selectedRow: [], data: "", text: "", ... };
/** @type {{data: Array, loading: boolean, ...}} */
var query1 = { data: [], loading: false, ... };
/** @type {string} */
var currentUser = "...";
var _ = {};
var dayjs = {};
```

这样 Tern.js 就能理解 `table1.selectedRow` 的类型为 `Array`，并提供正确的属性补全。

#### 序列化截断策略

为防止大数据量导致性能问题，序列化时严格限制：

| 限制项 | 上限 |
|--------|------|
| 数组元素 | 最多 10 个 |
| 对象嵌套 | 最多 3 层 |
| 字符串长度 | 最多 100 字符 |
| 对象属性 | 最多 50 个 |

#### 定义文件

| 文件 | 内容 |
|------|------|
| `defs/ecmascript.json` | ECMAScript 内置对象类型定义 |
| `defs/ecmascript-zh.json` | 中文版 |
| `defs/lodash.json` | Lodash 方法类型定义 |
| `defs/lodash-zh.json` | 中文版 |

### 3. SQLCompletionSource — SQL 补全

**文件**: `base/codeEditor/completion/sqlCompletionSource.tsx`

**职责**: SQL 关键字补全 + 表名/列名元数据补全。

| 优先级 | 关键字 |
|--------|--------|
| `boost: 2` | `select`, `from`, `where`, `and`, `or`, `insert`, `update`, `delete`, `create`, `drop`, `alter` |

### 4. CompletionSource 抽象基类

**文件**: `base/codeEditor/completion/completion.tsx`

```typescript
export abstract class CompletionSource {
  protected isFunction?: boolean;
  abstract completionSource(context: CompletionContext): 
    CompletionResult | Promise<CompletionResult | null> | null;
}
```

## 补全数据的采集与传递

### 数据采集链路

```
RootComp
  │
  └─ nameAndExposingInfo()          [rootComp.tsx]
       ├─ ui.nameAndExposingInfo()        → UI 组件（按钮、表格、输入框等）
       ├─ queries.nameAndExposingInfo()   → 查询（SQL、API 等）
       ├─ hooks.nameAndExposingInfo()     → Hook 组件（弹窗、抽屉等）
       ├─ tempStates.nameAndExposingInfo() → 临时状态
       ├─ transformers.nameAndExposingInfo() → 转换器
       └─ dataResponders.nameAndExposingInfo() → 数据响应器
            │
            ▼
       EditorState                      [editorState.tsx]
            │
            ▼
       exposingDataForAutoComplete()    [exposingTypes.tsx]
            │
            ▼
       Record<string, unknown>          → 传给 CodeEditor 的 exposingData prop
```

### exposingDataForAutoComplete 变换

```typescript
// 从 NameAndExposingInfo → Record<string, unknown>
// 每个组件的 propertyValue 被提取出来
// 如果 includeMethods=true，还将方法合并到属性值中
{
  table1: { selectedRow: [], data: [...], text: "..." },
  query1: { data: [...], loading: false, ... },
  currentUser: { name: "...", email: "..." },
  _
}
```

### 全局变量

定义在 `comps/utils/globalExposing.tsx`：

| 变量 | 来源 | 说明 |
|------|------|------|
| `currentUser` | 后端 | 当前用户信息 |
| `_` | Lodash | 工具库 |
| `dayjs` | dayjs | 日期处理 |
| `uuid` | uuid | UUID 生成 |
| `numbro` | numbro | 数字格式化 |
| `Papa` | PapaParse | CSV 解析 |

`uuid`、`numbro`、`Papa` 还注册在 `constants/libConstants.ts` 中，用于 ESLint 校验时抑制"未定义变量"误报。

## 组件属性/方法的补全示例

### 属性补全

用户在 JavaScript 编辑器中输入 `table1.` 时：

```
1. 用户输入 "table1."
2. ExposingCompletionSource.matchBefore() 
   → 匹配到 "table1" 作为前缀路径
3. getDataInfo(exposingData, "table1")
   → evalScript("table1", exposingData)
   → 得到 table1 对象的所有键
4. 返回补全列表（含优先级排序）:
   - selectedRow (boost: 10) ← 优先级属性
   - data (boost: 10)
   - text (boost: 10)
   - value (boost: 10)
   - onChange
   - ...其他属性
5. TernServer 同时验证类型，提供更精确的子属性补全
```

### 查询返回值的补全

```
输入 "query1."
  1. 获取 query1 的 exposing 数据
  2. query1.data        ← 查询结果数组
  3. query1.loading     ← 加载状态
  4. query1.code        ← 查询代码
  5. 后续输入 "query1.data[0]." 时，eval 路径会进一步深入

输入 "query1.data[0]."
  1. getDataInfo(data, "query1.data[0]")
  2. evalScript("query1.data[0]", exposingData)
  3. 遍历数组元素，提取第一个对象的键
```

### 方法的补全

当 `evalWithMethods=true` 时（组件属性面板编辑器中），组件的操作方法被合并到 exposing 数据中：

```
输入 "table1."
  补全列表包含方法:
  - table1.setValue(v)    (boost: 10) ← 优先级方法
  - table1.setData(v)     (boost: 10)
  - table1.onChange       ← 普通属性
```

## 响应式依赖追踪

当用户编写代码时，系统通过 `parseDepends()` 解析出代码依赖了哪些组件/查询，建立响应式依赖图：

**文件**: `barda-core/src/eval/utils/evaluate.tsx`

```typescript
parseDepends(code)
  // 解析出代码中引用的所有变量名
  // 与 exposingData 的键匹配
  // 建立依赖关系：当 table1.selectedRow 变化时，重新执行该表达式
```

## 其他编辑器功能

### 组件名点击跳转

**文件**: `base/codeEditor/clickCompName.tsx`

使用 CodeMirror 的 `StateField` + `Decoration` 高亮组件名。点击可跳转到编辑器中对应的组件位置。

### ESLint 集成

**文件**: `base/codeEditor/extensions.tsx` (line 428-523)

- 使用 `eslint4b-prebuilt-2` 在浏览器中运行 ESLint
- ECMAScript 2022 规则
- `markerFilter` 抑制 exposing 数据中的标识符和库名（`uuid`, `numbro`, `Papa`）的"未定义"错误

### 求值结果高亮

**文件**: `base/codeEditor/extensions/highlightJsExtension.tsx`

使用 `Decoration.mark` 将 `{{...}}` 段落的求值结果高亮为绿色（成功）或红色（错误）。

### 图标内联渲染

**文件**: `base/codeEditor/extensions/iconExtension.tsx`

将 `"/icon:icon-name"` 文本替换为渲染的 SVG 图标 Widget。

## 完整文件清单

| 文件路径 | 职责 |
|----------|------|
| `barda/src/base/codeEditor/codeEditor.tsx` | 主 CodeEditor React 组件，`useCodeMirror()` hook |
| `barda/src/base/codeEditor/codeEditorTypes.tsx` | 类型定义：`Language`, `CodeEditorProps` |
| `barda/src/base/codeEditor/extensions.tsx` | 扩展编排：语言扩展、补全源、语法高亮、格式化 |
| `barda/src/base/codeEditor/codeEditorUtils.tsx` | `checkCursorInBinding()`, `transformCompInfoIntoRecord()` |
| `barda/src/base/codeEditor/autoFormat.tsx` | 自动格式化（Prettier + sql-formatter） |
| `barda/src/base/codeEditor/clickCompName.tsx` | 组件名点击高亮 |
| `barda/src/base/codeEditor/completion/completion.tsx` | CompletionSource 抽象基类 |
| `barda/src/base/codeEditor/completion/exposingCompletionSource.tsx` | **应用上下文补全源**（组件、查询等） |
| `barda/src/base/codeEditor/completion/ternServer.tsx` | **Tern.js 语法补全源**（内置 API、类型推断） |
| `barda/src/base/codeEditor/completion/sqlCompletionSource.tsx` | SQL 关键字 + 元数据补全 |
| `barda/src/base/codeEditor/completion/cssCompletionSource.ts` | CSS 补全（当前未激活） |
| `barda/src/base/codeEditor/completion/defs/ecmascript.json` | ECMAScript 类型定义 |
| `barda/src/base/codeEditor/completion/defs/lodash.json` | Lodash 类型定义 |
| `barda/src/base/codeEditor/completion/defs/ecmascript-zh.json` | 中文版 ECMAScript 定义 |
| `barda/src/base/codeEditor/completion/defs/lodash-zh.json` | 中文版 Lodash 定义 |
| `barda/src/comps/utils/exposingTypes.tsx` | `exposingDataForAutoComplete()` |
| `barda/src/comps/utils/globalExposing.tsx` | 全局变量（currentUser, _, dayjs） |
| `barda/src/comps/generators/withExposing.tsx` | 组件暴露声明体系（NameConfig, depsConfig） |
| `barda/src/comps/editorState.tsx` | EditorState 缓存 `nameAndExposingInfo()` |
| `barda/src/comps/controls/codeControl.tsx` | 代码控件，将 exposingData 传递给 CodeEditor |
| `barda/src/comps/controls/codeTextControl.tsx` | 纯文本代码控件 |
| `barda/src/comps/comps/rootComp.tsx` | RootComp 收集所有子组件/查询的 exposing 数据 |
| `barda-core/src/eval/utils/evaluate.tsx` | `filterDepends()`, `parseDepends()` 依赖提取 |
| `barda-core/src/eval/utils/evalScript.tsx` | `evalScript()`, `proxySandbox()` 沙箱执行 |
| `barda-core/src/eval/codeNode.tsx` | CodeNode 代码节点求值 |
