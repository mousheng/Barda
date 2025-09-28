本文档介绍在Barda中通过编写[自定义 CSS](../style.md#%E8%87%AA%E5%AE%9A%E4%B9%89css) 代码实现丰富样式的注意事项。

1. 编写自定义 CSS 代码时，请尽量使用如下 CSS 选择器：

|类名|描述|
| ----------------| --------------|
|top-header|顶部导航|
|root-container|应用顶层容器|

2. 每个组件的组件名称会作为当前组件容器的类名。例如：当用户拖入一个名称为 `text1`​ 的文本组件时，用户可以通过 `.text1`​ 类选择器来定义 `text1`​ 组件的样式。
3. 每一种组件具有一个相同的类名，命名格式为 `ui-comp-{COMP_TYPE}`​ 。例如，可以通过 `.ui-comp-select`​ 来设置所有选择器组件的样式。所有支持的组件的类型列表如下：

```plain
input
textArea
password
richTextEditor
numberInput
slider
rangeSlider
rating
switch
select
multiSelect
cascader
checkbox
radio
segmentedControl
file
date
dateRange
time
timeRange
button
link
dropdown
toggleButton
text
table
image
progress
progressCircle
fileViewer
divider
qrCode
form
jsonSchemaForm
container
tabbedContainer
modal
listView
navigation
iframe
custom
module
jsonExplorer
jsonEditor
tree
treeSelect
audio
video
drawer
carousel
collapsibleContainer
chart
imageEditor
scanne
```

4. 除以上所述外，请避免使用其他类名，尤其是形如 `sc-dkiQaF bfTYCO`​ 的类名，极有可能在版本迭代过程中发生变化。
5. Barda的自定义 CSS 功能内置 [Stylis](https://stylis.js.org/) 轻量级 CSS 预处理工具。因此，开发者可使用嵌套的语法来提高效率，例如：

```scss
.text1 {
    span {
        color: red;
        font-weight: bold;
    }
}
```

自定义的 CSS 都会放到 `#app-{APP_ID}`​ 的命名空间内，模块的 CSS 会放到 `#module-{MODULE_ID}`​ 的命名空间内。

6. 如果设置的自定义 CSS 样式没有生效，很可能是[应用主题](../style.md#%E5%BA%94%E7%94%A8%E4%B8%BB%E9%A2%98)或[组件样式](../style.md#%E7%BB%84%E4%BB%B6%E6%A0%B7%E5%BC%8F)属性配置具有更高的优先级，因而覆盖了 CSS 样式。开发者可以通过浏览器的**审查元素 (Inspect)** 工具来自行排查。