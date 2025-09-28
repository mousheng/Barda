import { Node } from "../node";

/**
 * 向目标 Map 中添加一个依赖项。
 *
 * @param target - 要添加依赖项的 Map。
 * @param node - 要添加依赖项的节点。
 * @param paths - 要添加的路径。
 */
export function addDepend(
  target: Map<Node<unknown>, Set<string>>,
  node: Node<unknown> | undefined,
  paths: string[] | Set<string>
) {
  // 如果节点未定义，则返回
  if (!node) {
    return;
  }

  // 获取节点在 Map 中的值
  let value = target.get(node);

  // 如果值未定义，则创建一个新的 Set，并将其添加到 Map 中
  if (value === undefined) {
    value = new Set();
    target.set(node, value);
  }

  // 将路径添加到 Set 中
  paths.forEach((p) => value?.add(p));
}

/**
 * 将来自 source Map 的所有依赖项添加到 target Map 中。
 *
 * @param target - 要添加依赖项的 Map。
 * @param source - 要从中添加依赖项的 Map。
 * @returns 已添加了所有依赖项的 target Map。
 */
export function addDepends(
  target: Map<Node<unknown>, Set<string>>,
  source?: Map<Node<unknown>, Set<string>>
) {
  // 从 source Map 中添加所有依赖项
  source?.forEach((paths, node) => addDepend(target, node, paths));

  // 返回已添加了所有依赖项的 target Map
  return target;
}
