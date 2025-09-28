import { compile, serialize, middleware, prefixer, stringify } from "stylis";

function styleNamespace(id: string) {
  return `style-for-${id}`;
}

/**
 * 在页面上评估并应用给定ID的样式规则
 * 
 * 此函数通过接受一个ID和一个CSS字符串数组，生成相应的样式节点并将其添加到页面中
 * 它处理CSS规则的编译和序列化，跳过空的CSS字符串，使用预定义的中间件处理CSS，
 * 然后将编译后的CSS字符串赋给样式节点，从而在浏览器中生效
 * 
 * @param id 样式的ID，用于生成唯一的样式节点ID和选择器
 * @param css CSS规则数组，每个规则作为一个数组元素
 */
export function evalStyle(id: string, css: string[]) {
  // 根据给定的ID生成样式节点的唯一ID
  const styleId = styleNamespace(id);

  // 初始化编译后的CSS字符串
  let compiledCSS = "";
  
  // 遍历CSS规则数组，编译非空的CSS规则
  css.forEach((i) => {
    if (!i.trim()) {
      // 如果CSS规则为空，则跳过
      return;
    }
    // 编译和序列化CSS规则，然后添加到编译后的CSS字符串中
    compiledCSS += serialize(compile(`#${id}{${i}}`), middleware([prefixer, stringify]));
  });

  // 尝试查询已存在的样式节点，如果没有找到则创建一个新的样式节点
  let styleNode = document.querySelector(`#${styleId}`);
  if (!styleNode) {
    styleNode = document.createElement("style");
    styleNode.setAttribute("type", "text/css");
    styleNode.setAttribute("id", styleId);
    styleNode.setAttribute("data-style-src", "eval");
    // 将样式节点添加到文档的头部
    document.querySelector("head")?.appendChild(styleNode);
  }
  
  // 将编译后的CSS字符串赋给样式节点，使其在浏览器中生效
  styleNode.textContent = compiledCSS;
}

/**
 * 清除内联样式评价函数
 * 该函数用于清除特定的内联样式，如果未指定ID，则清除所有匹配的数据源样式节点
 * 
 * @param id 可选参数，指定要清除的样式的ID如果未提供，则清除所有通过eval方式注入的样式
 */
export function clearStyleEval(id?: string) {
  // 如果提供了ID，则计算该ID的样式命名空间值
  const styleId = id && styleNamespace(id);
  // 查询所有通过eval方式注入的样式节点
  const styleNode = document.querySelectorAll(`style[data-style-src=eval]`);
  // 遍历并清除匹配的样式节点
  if (styleNode) {
    styleNode.forEach((i) => {
      // 如果没有指定ID，或者当前节点的ID与计算出的样式ID匹配，则移除该节点
      if (!styleId || styleId === i.id) {
        i.remove();
      }
    });
  }
}
