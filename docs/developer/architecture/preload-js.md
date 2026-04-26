# 预加载 JS 机制

## 概述

预加载 JS 机制确保在应用组件树渲染 **之前**，完成 CSS 注入、第三方库加载、自定义脚本执行等初始化工作。页面在预加载完成前显示空白/加载状态，预加载完成后应用才真正渲染。

## 核心流程

```
应用启动
  ↓
RootComp.preload(id)              ← 入口 (rootComp.tsx)
  ↓
PreloadComp.run(id)               ← 编排 (preLoadComp.tsx)
  ├── 1. CSS 注入
  │     └─ CSSComp.run() → evalStyle()
  │        → Stylis 编译 CSS（前缀补全、序列化）
  │        → 注入 <style id="style-for-{id}"> 到 <head>
  │        → CSS 选择器限定在 #id 范围内
  │
  ├── 2. JS 库加载
  │     └─ LibsComp.run() → loadAllLibs()
  │        → 合并组织级和应用级库列表
  │        → 逐个 fetch(url) → evalFunc(code, proxySandbox)
  │        → 在沙箱中执行，通过 onSetGlobalVars 跟踪全局变量
  │        → 支持宿主模式（runInHost=true，用 <script> 标签绕过沙箱）
  │
  └── 3. 自定义 JS 脚本
        └─ ScriptComp.run() → runScript(code, runInHost)
           → 沙箱模式：evalFunc(code) 在 proxySandbox 中执行
           → 宿主模式：document.createElement('script') 注入真实标签
  ↓
RootComp 设置 preloaded=true → 应用渲染
```

## 沙箱执行引擎 (evalScript.tsx)

### 实现原理

通过 `new Function()` + `with(this)` + `Proxy` 创建隔离执行环境：

```javascript
// 核心模式
new Function('context', 'code', 'with(context) { return eval(code) }')
```

`proxySandbox` 是一个 `Proxy` 封装的虚拟 `window` 对象：

### 安全限制

| 策略 | 说明 |
|------|------|
| **黑名单拦截** | 函数作用域禁止 `document`, `location`, `fetch`, `top`, `parent`, `XMLHttpRequest`, `MutationObserver`, `chrome` |
| **表达式附加限制** | 额外禁止 `setTimeout`, `setInterval`, `setImmediate` |
| **黑洞 Proxy** | 访问黑名单属性返回一个黑洞对象，所有操作静默失败 |
| **has 陷阱** | 始终返回 `true`，防止代码通过 `'xxx' in window` 检测沙箱 |
| **set 陷阱** | 阻止覆盖上下文变量和受保护的全局属性 |

### 宿主模式

当 `runInHost=true`（组织级设置），沙箱被完全绕过，代码通过 `document.createElement('script')` 创建真实 `<script>` 标签注入到页面（commonUtils.ts 的 `runScriptInHost()` 方法）。

## 两级配置

| 级别 | 存储位置 | 作用范围 | 管理入口 |
|------|----------|----------|----------|
| **组织级** | 后端 `orgCommonSettings`（CommonSettingApi） | 组织下所有应用 | 管理后台 → 高级设置 |
| **应用级** | 应用 DSL（编辑器侧边栏） | 单个应用 | 编辑器 → 设置 → 其他 |

`PreloadComp.run()` 将两者合并执行：
- `externalLibs + appLibs` → LibsComp（去重后加载）
- `externalCSS + css` → CSSComp
- `externalScript + script` → ScriptComp

## 关键文件

| 文件路径 | 职责 |
|----------|------|
| `client/packages/barda/src/comps/comps/preLoadComp.tsx` | PreloadComp 组件类，编排预加载全流程 |
| `client/packages/barda/src/comps/comps/rootComp.tsx` | RootComp 入口，preload() 方法触发预加载 |
| `client/packages/barda/src/appView/AppView.tsx` | 运行时入口，initHandler 触发预加载 |
| `client/packages/barda/src/pages/editor/appEditorInternal.tsx` | 编辑器入口，initHandler 触发预加载 |
| `client/packages/barda/src/pages/editor/editorView.tsx` | 编辑器视图，传递 preloadComp 属性 |
| `client/packages/barda/src/pages/editor/left/settingsPanel.tsx` | 设置侧边栏，渲染预加载配置 UI |
| `client/packages/barda/src/pages/setting/advanced/AdvancedSetting.tsx` | 组织级预加载设置页面 |
| `client/packages/barda/src/api/commonSettingApi.ts` | 组织设置 API |
| `client/packages/barda-core/src/eval/utils/evalScript.tsx` | **沙箱执行引擎** — evalFunc, proxySandbox |
| `client/packages/barda-core/src/eval/utils/evalStyle.ts` | **CSS 注入引擎** — evalStyle, clearStyleEval |
| `client/packages/barda/src/util/commonUtils.ts` | runScriptInHost（宿主模式执行） |
| `client/packages/barda/src/redux/reducers/uiReducers/commonSettingsReducer.ts` | 组织设置 Redux 状态 |
| `client/packages/barda/src/comps/utils/globalSettings.ts` | 全局设置单例 |

## 数据流图

```
┌─────────────────────────────────────────────────────────┐
│                    应用启动 / 编辑器打开                    │
│                            │                             │
│                    initHandler(cb)                       │
│                            │                             │
│                  useCompInstance 调用                     │
│                    组件树还未渲染                          │
│                            │                             │
│                   RootComp.preload(id)                   │
│                    preloaded = false                     │
│                    getView() → null                      │
│                            │                             │
│  ┌─────────────────────────┼─────────────────────────┐   │
│  │                         │                         │   │
│  ▼                         ▼                         ▼   │
│ CSSComp.run()        LibsComp.run()           ScriptComp  │
│  │                       │                       .run()   │
│  │                       │                         │      │
│  ▼                       ▼                         ▼      │
│ evalStyle()         fetch + evalFunc()         evalFunc() │
│ Stylis 编译          proxySandbox 执行        or scriptTag│
│ <style> 注入         跟踪全局变量              注入执行     │
│                              │                            │
│  ┌───────────────────────────┘                            │
│  ▼                                                       │
│ 所有预加载完成                                            │
│  │                                                       │
│  ▼                                                       │
│ RootComp.preloaded = true                                │
│ getView() → <RootView> 应用渲染                           │
└─────────────────────────────────────────────────────────┘
```
