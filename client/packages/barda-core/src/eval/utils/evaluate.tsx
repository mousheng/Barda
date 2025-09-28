import _ from "lodash";
import { Node } from "../node";
import { addDepends, addDepend } from "./dependMap";
import { nodeIsRecord } from "./nodeUtils";
import { getDynamicStringSegments, isDynamicSegment } from "./segmentUtils";

/**
 * 过滤并收集依赖
 * 
 * 该函数旨在处理一个未评估的字符串，从中提取动态段并分析依赖关系
 * 它会遍历给定字符串中的动态段，解析并收集这些动态段所依赖的节点
 * 
 * @param unevaledValue 未评估的字符串，可能包含动态段
 * @param exposingNodes 包含潜在依赖节点的记录，键为节点标识，值为节点对象
 * @param maxDepth 可选参数，指定解析依赖的最大深度，用于限制解析的复杂度
 * @returns 返回一个Map对象，其中键为Node对象，值为字符串的Set集合，表示每个节点所依赖的标识集合
 */
export function filterDepends(
  unevaledValue: string,
  exposingNodes: Record<string, Node<unknown>>,
  maxDepth?: number
) {
  // 初始化一个Map用于存储依赖关系，键为Node对象，值为依赖标识的Set集合
  const ret = new Map<Node<unknown>, Set<string>>();
  // 遍历未评估字符串中的动态段
  for (const segment of getDynamicStringSegments(unevaledValue)) {
    // 检查当前段是否为动态段
    if (isDynamicSegment(segment)) {
      // 解析动态段内的依赖并添加到结果Map中
      addDepends(ret, parseDepends(segment.slice(2, -2), exposingNodes, maxDepth));
    }
  }
  // 返回收集到的依赖关系Map
  return ret;
}

/**
 * 检查给定的段中是否存在循环依赖
 * 
 * @param segment - 待检查的段，其格式应表明允许动态解析
 * @param exposingNodes - 一个映射，键为节点标识符，值为节点实例，表示已暴露的节点
 * @returns 返回一个布尔值，表示给定段中是否存在循环依赖
 */
export function hasCycle(segment: string, exposingNodes: Record<string, Node<unknown>>): boolean {
  // 如果段不是动态段，则直接返回false，因为静态段不存在循环依赖的问题
  if (!isDynamicSegment(segment)) {
    return false;
  }

  // 初始化返回值为false，表示尚未发现循环依赖
  let ret = false;

  // 去除段的动态标识符“**”前后的内容，然后解析依赖关系
  // 对解析出的每个节点，检查其是否存在循环依赖
  // 一旦发现循环依赖，立即标记返回值为true，并结束检查
  parseDepends(segment.slice(2, -2), exposingNodes).forEach((paths, node) => {
    ret = ret || node.hasCycle();
  });

  // 返回最终的检查结果
  return ret;
}

/**
 * 修改依赖项的名称
 * 
 * 此函数用于在给定的字符串中重命名某个依赖项的名称它支持两种模式：函数模式和非函数模式
 * 在函数模式下，整个字符串作为函数体进行重命名；在非函数模式下，仅对字符串中的动态段进行重命名
 * 
 * @param unevaledValue 未评估的字符串值，可能是函数体或包含动态段的字符串
 * @param oldName 需要被替换的原始依赖项名称
 * @param name 新的依赖项名称
 * @param isFunction 可选参数，指示是否将整个字符串作为函数体进行重命名，默认为false
 * @returns 修改后的字符串如果输入参数无效，则返回原始字符串
 */
export function changeDependName(
  unevaledValue: string,
  oldName: string,
  name: string,
  isFunction?: boolean
) {
  // 检查输入参数是否有效，如果有任何一个参数无效，则直接返回原始字符串
  if (!unevaledValue || !oldName || !name) {
    return unevaledValue;
  }

  // 如果是指定作为函数体进行重命名，则直接调用rename函数进行替换
  if (isFunction) {
    return rename(unevaledValue, oldName, name);
  }

  // 对于非函数模式，将字符串分割成动态段和非动态段，分别处理
  // 首先获取字符串中的动态段数组
  return getDynamicStringSegments(unevaledValue)
    .map((segment) => {
      // 如果当前段不是动态段，则直接返回该段
      if (!isDynamicSegment(segment)) {
        return segment;
      }
      // 如果当前段是动态段，则对该段进行重命名
      return rename(segment, oldName, name);
    })
    .join("");
}

/**
 * 重命名函数，用于替换字符串中指定的标识符
 * 
 * 此函数的目的是在给定的字符串段中，将所有的旧标识符替换为新标识符
 * 标识符可以是简单的名称，也可以是包含点或方括号访问的嵌套属性
 * 
 * @param segment 待处理的字符串段，可能包含需要替换的旧标识符
 * @param oldName 需要被替换的旧标识符
 * @param name 新的标识符，将替换旧标识符
 * @returns 返回替换旧标识符后的新字符串段
 */
function rename(segment: string, oldName: string, name: string) {
  // 定义访问符，用于匹配点和方括号访问
  const accessors = [".", "["];
  // 定义正则表达式字符串列表，用于匹配标识符
  const regStrList = ["[a-zA-Z_$][a-zA-Z_$0-9.[\\]]*", "\\[[a-zA-Z_][a-zA-Z_0-9.]*"];

  let ret = segment;
  // 遍历正则表达式字符串列表
  for (const regStr of regStrList) {
    // 创建全局匹配的正则表达式
    const reg = new RegExp(regStr, "g");
    // 使用正则表达式替换匹配到的标识符
    ret = ret.replace(reg, (s) => {
      // 如果匹配到的标识符与旧标识符相同，则直接返回新标识符
      if (s === oldName) {
        return name;
      }
      let origin = oldName;
      let target = name;
      let matched = false;

      // 检查标识符是否以指定的旧标识符作为开头，并进行替换
      if (s.startsWith(`[${origin}`)) {
        origin = `[${origin}`;
        target = `[${name}`;
        matched = true;
      }

      // 遍历访问符，检查并替换符合条件的标识符
      for (const accessor of accessors) {
        if (s.startsWith(origin + accessor)) {
          matched = true;
          target = target + accessor + s.substring(origin.length + accessor.length);
          break;
        }
      }

      // 如果匹配成功，则返回替换后的标识符
      if (matched) {
        return target;
      }

      // 如果没有匹配到，返回原字符串
      return s;
    });
  }

  // 返回替换完成的字符串段
  return ret;
}

/**
 * 从给定的JavaScript代码片段中提取标识符
 * 标识符可以是变量名、属性访问等，符合JavaScript的标识符命名规则
 * 此函数递归处理嵌套的属性访问和索引
 * 
 * @param jsSnippet 输入的JavaScript代码片段字符串
 * @returns 提取的标识符数组如果未找到标识符，则返回原始输入数组
 */
function getIdentifiers(jsSnippet: string): string[] {
  // 存储最终提取的标识符数组
  const ret: string[] = [];
  // 使用正则表达式匹配常见的标识符（不包括索引）
  const commonReg = /[a-zA-Z_$][a-zA-Z_$0-9.[\]]*/g;
  // 匹配输入字符串中的所有常见标识符
  const commonIds = jsSnippet.match(commonReg);
  if (commonIds) {
    // 将所有匹配到的常见标识符添加到结果数组中
    ret.push(...commonIds);
  }

  // 存储匹配到的索引标识符数组
  const indexIds: string[] = [];
  // 使用正则表达式匹配索引标识符，如 `[identifier]`
  (jsSnippet.match(/\[[a-zA-Z_][a-zA-Z_0-9\[\].]*\]/g) || []).forEach((i) => {
    // 递归处理索引标识符内部的子标识符
    indexIds.push(...getIdentifiers(i.slice(1, -1)));
  });
  // 将索引标识符添加到结果数组中
  ret.push(...indexIds);

  // 如果未找到任何标识符，则返回包含原始输入的数组
  if (ret.length === 0) {
    return [jsSnippet];
  }
  // 返回最终的标识符数组
  return ret;
}

/**
 * 解析代码片段中的依赖关系
 * 
 * 此函数旨在从给定的JavaScript代码片段中解析出所有依赖项它通过分析代码片段中的标识符，
 * 并根据这些标识符在提供的节点对象中查找对应的依赖项该函数可以限制依赖路径的最大深度，
 * 以便在解析时忽略掉过于深层的依赖
 * 
 * @param jsSnippet JavaScript代码片段
 * @param exposingNodes 包含所有可能的依赖项的节点对象，键为节点名称，值为对应的节点对象
 * @param maxDepth 依赖路径的最大深度，可选参数，用于限制解析依赖时的最大深度
 * @returns 返回一个Map对象，其中键是依赖项的节点对象，值是一个字符串的Set集合，表示该节点的依赖项
 */
function parseDepends(
  jsSnippet: string,
  exposingNodes: Record<string, Node<unknown>>,
  maxDepth?: number
) {
  // 创建一个Map来存储解析出的依赖关系，键为依赖项的节点对象，值为依赖项的名称集合
  const depends = new Map<Node<unknown>, Set<string>>();

  // 获取JavaScript代码片段中的所有标识符
  const identifiers = getIdentifiers(jsSnippet);

  // 遍历所有标识符，为每个标识符解析依赖
  identifiers.forEach((identifier) => {
    // 将标识符转换为路径形式
    const subpaths = _.toPath(identifier);

    // 根据最大深度限制和路径获取依赖项的节点
    const depend = getDependNode(maxDepth ? subpaths.slice(0, maxDepth) : subpaths, exposingNodes);

    // 如果找到了依赖项，则添加到依赖集合中
    if (depend) {
      addDepend(depends, depend[0], [depend[1]]);
    }
  });

  // 返回解析出的依赖关系Map
  return depends;
}

/**
 * 根据子路径和暴露的节点集合，获取依赖的节点信息
 * 
 * 此函数的目的是在一个层级结构中，根据给定的路径片段（subPaths）
 * 查找对应的节点（node）以及其完整路径。它会在提供的暴露节点集合（exposingNodes）
 * 中递层深入地查找，直到找到最终的节点或遍历完所有路径片段
 * 
 * @param subPaths 字符串数组，表示路径的各个层级的标识符
 * @param exposingNodes 记录类型，键是字符串，值为泛型Node，表示可访问的节点集合
 * @returns 返回找到的节点及其路径字符串，如果未找到则返回undefined
 */
function getDependNode(
  subPaths: string[],
  exposingNodes: Record<string, Node<unknown>>
): [Node<unknown>, string] | undefined {
  // 当路径为空时，直接返回undefined
  if (subPaths.length <= 0) {
    return undefined;
  }

  // 初始化待检查的节点集合为暴露的节点集合
  let nodes = exposingNodes;
  // 用于存储最终找到的节点
  let node = undefined;
  // 存储成功匹配的路径片段
  const path = [];

  // 遍历路径片段，尝试逐层查找节点
  for (const subPath of subPaths) {
    // 尝试获取当前路径片段对应的子节点
    const subNode = nodes[subPath];
    // 如果当前节点集合中不存在该路径片段，或者对应的子节点不存在，则停止查找
    if (!nodes.hasOwnProperty(subPath) || !subNode) {
      break;
    }
    // 更新当前节点为找到的子节点
    node = subNode;
    // 将当前路径片段添加到已匹配路径中
    path.push(subPath);
    // 如果当前节点不是记录类型（即不再是层级结构的中间节点），则停止查找
    if (!nodeIsRecord(node)) {
      break;
    }
    // 更新待检查的节点集合为当前节点的子节点集合
    nodes = node.children;
  }

  // 根据查找结果，返回节点及其路径字符串，如果未找到则返回undefined
  return node ? [node, path.join(".")] : undefined;
}
