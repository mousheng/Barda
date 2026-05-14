import { StandardBoxMargin } from "@barda/comps/controls/styleControlConstants";
import { layoutsNodeItem } from "components/selectedComps";
import { UICompType } from "comps/uiCompRegistry";
import _ from "lodash";
import React, { ReactElement, SyntheticEvent } from "react";
import { DraggableEvent } from "react-draggable";
import { PositionParams } from "./calculateUtils";
import { draggingUtils } from "./draggingUtils";
import { ResizeHandleAxis } from "./gridLayoutPropTypes";

export type LayoutItem = {
  w: number;
  h: number;
  x: number;
  y: number;
  z?: number;
  i: string;
  minW?: number;
  minH?: number;
  maxW?: number;
  maxH?: number;
  static?: boolean;
  // 允许拖动
  isDraggable?: boolean;
  // 允许调整大小
  isResizable?: boolean;
  // 正在拖动：拖动项目具有更高的碰撞优先级
  isDragging?: boolean;
  // 隐藏：只占据位置，不显示，也不发生碰撞
  hide?: boolean;
  // 占位：显示 div 框，但不加载子元素
  placeholder?: boolean;
  // 延迟冲突：主要解决容器碰撞问题
  delayCollision?: boolean;
  resizeHandles?: ResizeHandleAxis[];
};

export type ResizeInfo = {
  e: SyntheticEvent<Element>;
  node: HTMLElement;
  i: string;
}

export type ExtraItem = {
  name: string;
  compType: UICompType;
  autoHeight?: boolean;
  isSelected?: boolean;
  hidden?: boolean;
  margin?: StandardBoxMargin;
};
export type Layout = Record<string, LayoutItem>;
export type ExtraLayout = Record<string, ExtraItem>;
export type Position = {
  left: number;
  top: number;
  width: number;
  height: number;
};
export type PartialPosition = {
  left: number;
  top: number;
};
export type Size = {
  width: number;
  height: number;
};
export type GridDragEvent = {
  e: DraggableEvent;
  node: HTMLElement;
  newPosition: PartialPosition;
};
export type GridResizeEvent = {
  e: SyntheticEvent<Element>;
  node: HTMLElement;
  size: Size;
  handle: ResizeHandleAxis;
  x: number;
  y: number;
};

export type OnDragCallback = (
  layout: Layout,
  keys: Layout,
  e: React.DragEvent<HTMLElement>
) => void;
// 所有的回调函数都具有以下签名：(layout, oldItem, newItem, placeholder, e)。
export type EventCallback = (
  layout: Layout,
  oldItem: LayoutItem | undefined,
  newItem: LayoutItem | undefined,
  placeholder: LayoutItem | undefined,
  e: DraggableEvent,
  node: HTMLElement | undefined
) => void;

export type ResizeEventCallback = (
  layout: Layout,
  oldItem: LayoutItem | undefined,
  newItem: LayoutItem | undefined,
  placeholder: LayoutItem | undefined,
  e: SyntheticEvent<Element>,
  node: HTMLElement | undefined
) => void;

const isProduction = process.env.NODE_ENV === "production";

/**
* 返回布局的底部坐标。
*
* @param｛Array｝布局布局数组。
* @return｛Number｝底部坐标。
*/
export function bottom(layout: Layout): number {
  return _.max(_.map(layout, item => item.y + item.h)) || 0;
}

// 在一个布局中修改一个布局项。返回一个新的布局对象，
// 不会对原始对象做修改。其余所有布局项保持不变。
export function modifyLayout(layout: Layout, layoutItem: LayoutItem): Layout {
  // 兜底操作，如果 layoutItem 的 i 为空，则返回原始布局
  return layoutItem.i ? { ...layout, [layoutItem.i]: layoutItem } : layout;
}

//调用函数修改布局项。
//是否进行防御性克隆以确保布局不被修改。
export function withLayoutItem(
  layout: Layout,
  itemKey: string,
  cb: (arg0: LayoutItem) => LayoutItem
): [Layout, LayoutItem | undefined] {
  if (!layout.hasOwnProperty(itemKey)) {
    return [layout, undefined];
  }
  layout = _.mapValues(layout, (item) => (item.i === itemKey ? cb(cloneLayoutItem(item)) : item));
  return [layout, layout[itemKey]];
}

export function cloneLayoutItem(layoutItem: LayoutItem): LayoutItem {
  return { ...layoutItem };
}

/**
 * 比较 React 中的 `children` 有些困难。
 * 这种方法能够捕捉到键值、顺序和长度上的差异。
*/
export function childrenEqual(a: ReactElement[], b: ReactElement[]): boolean {
  const aKey = React.Children.map(a, (c) => c?.key);
  const bKey = React.Children.map(b, (c) => c?.key);
  return _.isEqual(aKey, bKey);
}

/**
 * 参见 `fastRGLPropsEqual.js`。
 * 我们希望这个函数运行尽可能快——因为它会被频繁调用——并且对新增的属性具有较强的适应性。因此我们没有调用 `lodash.isEqual`，
 * 因为它不太适合用来比较属性，而是使用了这个专门的函数结合 `preval` 来生成最快可能的比较函数，针对我们的特定属性进行优化。
*/
type FastRGLPropsEqual = (
  arg0: Record<string, any>,
  arg1: Record<string, any>,
  arg2: (...args: Array<any>) => any
) => boolean;


/**
 * 给定两个布局项，检查它们是否发生碰撞。
*/
export function collides(l1: LayoutItem, l2: LayoutItem): boolean {
  return (
    l1.i !== l2.i && // 不是同一个元素
    l1.x < l2.x + l2.w && // l1 没有完全位于 l2 的右侧
    l1.x + l1.w > l2.x && // l1 没有完全位于 l2 的左侧
    l1.y < l2.y + l2.h && // l1 没有完全位于 l2 的下方
    l1.y + l1.h > l2.y // l1 没有完全位于 l2 的上方
  );
}

/**
 * 给定一个布局，确保所有元素都位于其边界内。
 *
 * 修改布局项。
 *
 * @param  {Array} layout 布局数组。
 * @param  {Number} bounds 列数。
*/
export function correctBounds(layout: Layout, cols: number): Layout {
  for (const l of Object.values(layout)) {
    if (l.w > cols) l.w = cols;
    if (l.x + l.w > cols) l.x = cols - l.w;
  }
  return layout;
}

/**
 * 获取所有静态元素。
 * @param  {Array} layout 布局对象数组。
 * @return {Array}        静态布局项数组。
*/
export function getStatics(layout: Layout): Layout {
  return _.pickBy(layout, (l) => l.static);
}

export function setTransform({ top, left, width, height }: Position): Record<string, any> {
  // 将没有单位的值转换为 px
  const translate = `translate(${left}px, ${top}px)`;
  return {
    transform: translate,
    width: `${width}px`,
    height: `${height}px`,
    position: "absolute",
  };
}

/**
 * 使用 `initialLayout` 和子元素作为模板生成一个布局。
 * 缺失的条目将会被添加，多余的条目将会被截断。
 *
 * 不会修改 `initialLayout`。
 *
 * @param  {Array}  initialLayout 通过属性传递进来的初始布局。
 * @param  {String} breakpoint    当前响应式的断点。
 * @param  {?String} compact      压缩选项。
 * @return {Array}                工作布局。
*/
export function synchronizeLayoutWithChildren(
  initialLayout: Layout,
  stateChangedHs: Record<string, number>,
  children: ReactElement[],
  cols: number
): Layout {
  initialLayout = initialLayout || {};
  // Generate one layout item per child.
  const layout: Layout = {};
  React.Children.forEach(children, (child: ReactElement) => {
    // Child may not exist
    if (child?.key == null) return;
    // Don't overwrite if it already exists.
    const exists = initialLayout[child.key];
    if (exists) {
      if (stateChangedHs[child.key]) {
        exists.h = stateChangedHs[child.key];
      }
      layout[exists.i] = cloneLayoutItem(exists);
    } else {
      // Nothing provided: ensure this is added to the bottom
      // FIXME clone not really necessary here
      // layout[child.key] = cloneLayoutItem({
      //   w: 1,
      //   h: 1,
      //   x: 0,
      //   y: bottom(layout),
      //   i: String(child.key),
      // });
    }
  });
  // Correct the layout.
  const correctedLayout = correctBounds(layout, cols);
  // const cascadedLayout = cascade(correctedLayout);
  // log.debug("layout: synchronizeLayout. layout: ", cascadedLayout);
  return correctedLayout;
}

/**
 * 解决碰撞问题，处理碰撞情况。
 * - 首先确保静态元素与额外元素之间没有碰撞
 *   - 如果发生碰撞，则返回 `success: false`
 *   - 如果没有碰撞，则将额外元素添加到静态元素中
 * - 按 y 轴对布局进行排序
 * - 遍历布局中的元素
 *   - 如果某个元素与静态元素发生碰撞，则将其向下移动
 *   - 移动完毕后，将元素添加到静态元素中
 *
 * @note 假设除了额外元素外，布局中没有其他碰撞
 * @param layout 存在碰撞的布局
 * @param priorLayout 不应移动的元素，通常为拖拽中的元素
 * @returns 无碰撞的布局
*/
export function cascade(layout: Layout, priorLayout: Layout = {}): Layout {
  // log.debug("layout: cascade begin. layout: ", layout, " priorLayout: ", priorLayout);
  priorLayout = _.assignIn(
    priorLayout,
    _.mapValues(
      _.pickBy(layout, (item) => item.isDragging),
      (item) => ({ ...item, isDragging: undefined })
    )
  );
  layout = _.mapValues(layout, (item) => ({ ...item }));
  let staticLayout = getStatics(layout);
  staticLayout = { ...staticLayout, ...priorLayout };
  if (_.size(priorLayout) > 0) {
    staticLayout = cascade(staticLayout);
  }

  // sort items by y
  const sortedItems: LayoutItem[] = _.sortBy(Object.values(layout), (item) => item.y);
  // sort static items also by y, and dynamically maintain the order
  let sortedCollisionAreas: LayoutItem[] = _.sortBy(Object.values(staticLayout), (item) => item.y);

  const newLayout: Layout = {};
  for (const item of sortedItems) {
    let newItem;
    if (staticLayout.hasOwnProperty(item.i)) {
      newItem = staticLayout[item.i];
    } else if (!!item.hide) {
      newItem = item;
    } else {
      sortedCollisionAreas = shrinkLayoutByMinY(sortedCollisionAreas, item.y);
      let collisionArea: LayoutItem;
      [newItem, collisionArea] = moveToSolveCollisions(item, sortedCollisionAreas);
      insertWithOrders(sortedCollisionAreas, collisionArea);
    }
    newLayout[newItem.i] = newItem;
    // log.warn("loop item: ", newItem, " hide: ", item.hide, " sortedCollisionAreas: ", { ...sortedCollisionAreas }, " newLayout: ", { ...newLayout });
  }

  // log.debug("layout: cascade. layout: ", layout, " priorLayout: ", priorLayout, " newLayout: ", newLayout, " staticLayout: ", staticLayout);
  return newLayout;
}

// 忽略已迭代过的项目
function shrinkLayoutByMinY(items: LayoutItem[], y: number): LayoutItem[] {
  return items.filter((item) => item.y + item.h > y);
}

/**
 * 将 itemToMove 移动以解决碰撞问题，之后会创建一个碰撞区域。
 *
 * @return {[@movedItem, @collisionArea]}
 * @movedItem 已移动的项目
 * @collisionArea 在后续的碰撞解决过程中，此区域不应被占用
 *
 * FIXME: 添加单元测试
 */
function moveToSolveCollisions(
  itemToMove: LayoutItem,
  items: LayoutItem[]
): [LayoutItem, LayoutItem] {
  const newItem = cloneLayoutItem(itemToMove);
  let collisionArea = newItem;
  for (const item of items) {
    if (newItem.y + newItem.h < item.y) {
      break;
    }
    const deltaY = deltaYToSolveCollision(newItem, item);
    // log.warn("itemToMove: ", newItem, " item: ", item, " deltaY: ", deltaY);
    if (deltaY > 0) {
      collisionArea = { ...collisionArea, h: collisionArea.h + deltaY };
    }
    newItem.y += deltaY;
  }
  return [newItem, collisionArea];
}

// 移动 itemToMove 以解决碰撞问题
function deltaYToSolveCollision(itemToMove: LayoutItem, staticItem: LayoutItem): number {
  if (!collides(itemToMove, staticItem)) {
    return 0;
  }
  return staticItem.y + staticItem.h - itemToMove.y;
}

/**
 * 将新的布局项插入到布局项数组中，并按照特定顺序调整数组元素的位置
 * 
 * 此函数的目的是将新的布局项（newItem）插入到一个已存在的布局项数组（items）中
 * 插入后，数组中的元素将根据其x和y坐标重新排序，以维持一种特定的顺序
 * 这种排序逻辑是，首先根据y坐标升序排序，如果两个元素的y坐标相同，则根据x坐标升序排序
 * 
 * @param items LayoutItem类型的数组，代表已存在的布局项
 * @param newItem 要插入的新布局项
 */
function insertWithOrders(items: LayoutItem[], newItem: LayoutItem) {
  // 将新布局项添加到数组末尾
  items.push(newItem);
  let idx;
  // 从数组末尾开始向前遍历，找到新布局项的合适位置
  for (idx = items.length - 1; idx > 0; idx--) {
    // 获取当前遍历到的布局项
    const item = items[idx - 1];
    // 如果新布局项的y坐标小于当前项，或者y坐标相同但x坐标更小，则将当前项后移
    if (newItem.y < item.y || (newItem.y === item.y && newItem.x < item.x)) {
      items[idx] = item;
    } else {
      // 找到合适位置后，跳出循环
      break;
    }
  }
  // 将新布局项放置在其合适的位置
  items[idx] = newItem;
}

/**
 * 计算元素调整大小时的水平和垂直方向上的位移量
 * 
 * 此函数根据调整大小的句柄方向和水平、垂直方向上的变化量，计算出元素整体需要移动的水平和垂直方向上的位移量
 * 主要用于在拖动元素边缘进行大小调整时，计算元素新的位置
 * 
 * @param handle 调整大小的句柄方向，包含字符"w"(西)、"e"(东)、"n"(北)、"s"(南)
 * @param deltaW 元素在水平方向上的变化量，正值表示向右变宽，负值表示向左变窄
 * @param deltaH 元素在垂直方向上的变化量，正值表示向下变高，负值表示向上变矮
 * @returns 返回一个包含水平和垂直方向位移的对象，deltaX表示水平位移，deltaY表示垂直位移
 */
export function calcResizeXY(
  handle: string,
  deltaW: number,
  deltaH: number
): { deltaX: number; deltaY: number } {
  let deltaX = 0;
  let deltaY = 0;
  if (handle.indexOf("w") > -1) {
    deltaX -= deltaW;
  }
  if (handle.indexOf("n") > -1) {
    deltaY -= deltaH;
  }
  return { deltaX, deltaY };
}

/**
 * 根据拖动过程中的偏移量计算新的拖动位置
 * 
 * 此函数接收当前的拖动位置和水平、垂直方向上的偏移量，
 * 并计算出新的拖动位置这有助于在用户拖动界面元素时，
 * 动态计算并更新元素的位置
 * 
 * @param dragging 当前的拖动位置对象，包含top和left属性
 * @param deltaX 水平方向上的偏移量，正值向右，负值向左
 * @param deltaY 垂直方向上的偏移量，正值向下，负值向上
 * @returns 返回一个新的拖动位置对象，包含更新后的top和left属性
 */
export function getDraggingNewPosition(
  dragging: PartialPosition,
  deltaX: number,
  deltaY: number
): PartialPosition {
  // 计算新的top和left值
  let top = dragging.top + deltaY;
  let left = dragging.left + deltaX;

  // 返回包含新位置的对象
  return { top, left };
}

/**
 * 计算鼠标在画布中的相对位置
 * @param e 事件对象
 * @param node 节点
 * @param positionParams 位置参数
 * @param draggingItemPosition 拖拽项的位置
 * @param transformScale 缩放比例
 * @returns 偏移量
 */
export function calcOffset(
  e: React.DragEvent<HTMLElement>,
  node: HTMLDivElement,
  positionParams: PositionParams,
  draggingItemPosition: { x: number, y: number },
  transformScale: number = 1.0
): {
  offsetX: number;
  offsetY: number;
  x: number;
  y: number;
} {
  const parentRect = node.getBoundingClientRect();
  const offsetX = (e.clientX - parentRect.left - positionParams.containerPadding[0]) / transformScale;
  const offsetY = (e.clientY - parentRect.top - positionParams.containerPadding[1] + node.scrollTop) / transformScale;
  const { x, y } = calcOffsetInGrid(offsetX, offsetY, positionParams, draggingItemPosition);
  return { offsetX, offsetY, x, y };
}


/**
 * 计算拖动项在网格中的偏移量
 * @param offsetX 当前鼠标的水平偏移量
 * @param offsetY 当前鼠标的垂直偏移量
 * @param positionParams 网格的列宽、行高和列数
 * @param draggingItemPosition 当前拖动项的初始位置
 * @returns 计算后的网格位置
 */
export function calcOffsetInGrid(
  offsetX: number,
  offsetY: number,
  positionParams: PositionParams,
  draggingItemPosition: { x: number, y: number }
) {
  const { colWidth, rowHeight, cols } = positionParams;
  let { x, y } = draggingItemPosition;

  const deltaX = offsetX - x * colWidth;
  x += Math.ceil(deltaX / colWidth);

  const deltaY = offsetY - y * rowHeight;
  y += Math.ceil(deltaY / rowHeight);

  x = _.clamp(x, 0, cols - 1);
  y = _.clamp(y, 0, Infinity);

  return { x, y };
}

export function isOutOfBox(item: LayoutItem): boolean {
  return item.x < 0 || item.y < 0 || !!item.hide;
}

export function isItemDraggable(item: LayoutItem) {
  return _.isNil(item.isDraggable) || item.isDraggable;
}

export function isItemResizable(item: LayoutItem) {
  return _.isNil(item.isResizable) || item.isResizable;
}

const EMPTY_RESIZE_HANDLES: ResizeHandleAxis[] = [];
const SELECTED_AUTO_HEIGHT_HANDLES: ResizeHandleAxis[] = ["e", "w"];
const SELECTED_DEFAULT_HANDLES: ResizeHandleAxis[] = ["s", "n", "w", "e", "sw", "nw", "se", "ne"];

function getResizeHandles(isSelected?: boolean, autoHeight?: boolean): Array<ResizeHandleAxis> {
  if (!isSelected) return EMPTY_RESIZE_HANDLES;
  return autoHeight ? SELECTED_AUTO_HEIGHT_HANDLES : SELECTED_DEFAULT_HANDLES;
}

export function getItemResizeHandles(item: LayoutItem, extraItem?: ExtraItem): ResizeHandleAxis[] {
  return item.resizeHandles && _.size(item.resizeHandles) !== 0
    ? item.resizeHandles
    : getResizeHandles(extraItem?.isSelected, extraItem?.autoHeight);
}

export function canResizeRight(item: LayoutItem, extraItem?: ExtraItem) {
  return getItemResizeHandles(item, extraItem).some((t) => t.includes("e"));
}

export function canResizeBottom(item: LayoutItem, extraItem?: ExtraItem) {
  return getItemResizeHandles(item, extraItem).some((t) => t.includes("s"));
}

/**
 * build adjcent list if there're adjcent items below every item
 * 如果每个项目下面都有可调整项目，则建立可调整项目列表
 */
// export function getStickyItemMap(layout: Layout): Record<string, Set<string>> {
//   const pairs = Object.values(layout).map((item) => {
//     const stickyItems = Object.values(layout)
//       .filter(
//         (stickyItem) =>
//           item.y + item.h === stickyItem.y &&
//           item.x < stickyItem.x + stickyItem.w &&
//           stickyItem.x < item.x + item.w &&
//           item.i !== stickyItem.i
//       )
//       .map((stickyItem) => stickyItem.i);
//     return [item.i, new Set<string>(stickyItems)];
//   });
//   // console.log(_.fromPairs(pairs))
//   return _.fromPairs(pairs);
// }


/**
 * 大幅优化getStickyItemMap函数性能
 * TODO：缓存layout数据，减少排序次数，可以再次优化性能
 */
export function myGetStickyItemMap(layout: Layout): Record<string, Set<string>> {
  let result = {}
  let orderedLayout = _.orderBy(layout, ["y", "x"])
  for (let i = 0; i < orderedLayout.length; i++) {
    let lst = []
    for (
      let j = i + 1;
      j < orderedLayout.length &&
      orderedLayout[j].y <= orderedLayout[i].y + orderedLayout[i].h;
      j++
    ) {
      let item = orderedLayout[i]
      let stickyItem = orderedLayout[j]
      if (
        item.y + item.h === stickyItem.y &&
        item.x < stickyItem.x + stickyItem.w &&
        stickyItem.x < item.x + item.w
      ) {
        lst.push(stickyItem.i)
      }
    }
    _.set(result, orderedLayout[i].i, lst)
  }
  return result
}

/**
 * 修改布局中的固定项
 * 
 * 此函数主要用于更新布局中处于固定状态的项当一个项的位置或大小发生变化时，
 * 需要相应地调整与之相关的固定项的位置，以保持布局的正确性和一致性
 * 
 * @param layout 布局对象，包含了所有项的布局信息
 * @param changeItem 发生变化的项，包括其新的位置和大小信息
 * @param stickyItemMap 可选参数，记录了固定项的映射关系，用于调整固定项的位置
 * @returns 返回更新后的布局对象
 */
export function changeStickyItem(
  layout: Layout,
  changeItem: LayoutItem,
  stickyItemMap?: Record<string, Set<string>>
): Layout {
  // 更新布局中变化项的信息
  layout = { ...layout, [changeItem.i]: changeItem };

  // 如果没有指定固定项映射，则直接返回更新后的布局
  if (!stickyItemMap) {
    return layout;
  }

  // 初始化一个队列，用于处理与变化项相关的固定项
  const queue = [changeItem.i];

  // 遍历处理队列中的每一项，直到队列为空
  while (queue.length > 0) {
    // 从队列中取出下一个需要处理的项的键值
    const key = queue.shift() as string;
    // 获取当前处理的项的信息
    const item = layout[key];

    // 如果当前项在固定项映射中存在，则遍历其相关的固定项
    if (stickyItemMap.hasOwnProperty(key)) {
      // 遍历当前项的所有固定项
      stickyItemMap[key].forEach((stickyKey) => {
        // 获取固定项的信息
        const stickyItem = layout[stickyKey];
        // 如果固定项存在且没有超出边界，则更新其位置
        if (stickyItem && !isOutOfBox(stickyItem)) {
          // 更新固定项的最小Y坐标，以保持正确的上下关系
          const stickyMinY = calcStickyMinY(layout, stickyKey);
          // 更新固定项的位置，并将其加入到处理队列中，以便后续处理可能的进一步变化
          layout[stickyKey] = {
            ...stickyItem,
            y: Math.max(item.y + item.h, stickyMinY),
            // isDragging: true,
          };
          stickyItem.h > 0 && queue.push(stickyKey);
        }
      });
    }
  }

  // 返回更新后的布局
  return layout;
}

/**
 * 计算指定粘性项的最小Y坐标
 * 
 * 该函数通过遍历布局对象中所有的项，找出在指定粘性项上方的所有项，
 * 并计算这些项的下边界中的最大值作为粘性项的最小Y坐标
 * 
 * @param layout 布局对象，包含所有项的位置和尺寸信息
 * @param key 粘性项的键名，用于从布局对象中获取粘性项的信息
 * @returns 返回粘性项的最小Y坐标，如果没有符合条件的项，则返回0
 */
function calcStickyMinY(layout: Layout, key: string) {
  // 获取指定键名的粘性项
  const stickyItem = layout[key];
  // 如果没有找到粘性项，则直接返回0
  if (!stickyItem) return 0;

  // 遍历布局对象中的所有项，筛选出在粘性项上方且与粘性项有水平重叠的项
  return Object.values(layout)
    .filter((item) => {
      return (
        item.x < stickyItem.x + stickyItem.w &&
        stickyItem.x < item.x + item.w &&
        item.y + item.h <= stickyItem.y
      );
    })
    // 对筛选出的项，提取它们的下边界Y坐标
    .map((item) => {
      return item.y + item.h;
    })
    // 使用reduce函数找出所有下边界Y坐标中的最大值
    .reduce((prev, cur) => Math.max(prev, cur), 0);
}

/**
 * 将布局中的每个元素的坐标相对于最小的x和y坐标进行平移，使最小的坐标点移动到(0,0)
 * 
 * @param layout 布局对象，其中每个元素都具有x和y坐标属性
 * @returns 返回一个新的布局对象，其中每个元素的坐标都已平移
 */
export function moveToZero(layout: Layout): Layout {
  // 计算layout对象中所有元素的最小x坐标
  const minX = _.min(Object.values(layout).map((item) => item.x)) as number;
  // 计算layout对象中所有元素的最小y坐标
  const minY = _.min(Object.values(layout).map((item) => item.y)) as number;

  // 使用lodash的mapValues函数将layout对象中的每个元素的坐标进行平移
  return _.mapValues(layout, (item) => ({
    ...item,
    x: item.x - minX,
    y: item.y - minY,
  }));
}

/**
 * 计算粘贴的基准坐标
 * 
 * 此函数用于根据布局对象和可选的键名数组计算出粘贴操作的基准坐标（x, y）
 * 如果提供了键名数组，则仅考虑这些键进行计算；如果没有提供，则考虑布局对象中的所有键
 * 
 * @param layout 布局对象，包含了所有键的布局信息
 * @param keys 可选的键名数组，用于指定计算时要考虑的键
 * @returns 返回一个对象，包含 x 和 y 坐标，表示粘贴的基准位置
 */
export function calcPasteBaseXY(layout: Layout, keys?: string[]): { x: number; y: number } {
  // 根据keys数组选择性地提取layout对象中的部分属性，如果keys未指定，则提取layout的所有属性
  const filterLayout = _.pick(layout, keys ?? []);
  // 如果提取的布局对象为空，即没有键可以考虑，则将x坐标设为0，y坐标设为布局中最下方的y坐标
  if (_.size(filterLayout) === 0) {
    return { x: 0, y: bottom(layout) };
  }
  // 计算参与计算的键中的最小x坐标，如果不存在则默认为0
  const minX = _.min(Object.values(filterLayout).map((item) => item.x)) ?? 0;
  // 返回最小x坐标和参与计算的键中最下方的y坐标作为粘贴的基准坐标
  return { x: minX, y: bottom(filterLayout) };
}


/**
 * 计算布局中每个元素的左侧相邻元素，处理多选拖拽时的坐标偏移问题
 * 
 * 此函数用于找出布局中每个元素的左侧直接相邻的元素，并将结果以对象的形式返回，
 * 其中每个键是布局中的一个元素，对应的值是该元素左侧相邻元素的数组
 * 使用了 O(n^2) 的暴力实现方式。
 * 
 * @param layout 布局对象，其中每个元素都具有属性i(元素标识)，x(元素左边距)，y(元素顶部边距)，w(元素宽度)，h(元素高度)
 * @returns 返回一个对象，其中每个键值对表示布局中一个元素及其左侧相邻元素的数组
 */
export function calcLeftAdjacentItems(layout: Layout): Record<string, string[]> {
  // 对布局中的每个元素进行遍历
  return _.mapValues(layout, (item) => {
    // 筛选出所有在当前元素左侧且与之相邻的元素
    return Object.values(layout)
      .filter((leftItem) => {
        // 确保筛选的元素不在当前元素的左侧，且其右边框与当前元素的左边框重合
        // 同时，筛选的元素的高度必须与当前元素有一定的重叠
        return (
          leftItem.i !== item.i &&
          leftItem.x + leftItem.w === item.x &&
          leftItem.y + leftItem.h > item.y &&
          item.y + item.h > leftItem.y
        );
      })
      // 提取筛选出的元素的标识符
      .map((leftItem) => leftItem.i);
  });
}

/**
 * 通过平移保持布局内的项目不超出边界
 * 若无法实现，则裁剪右侧的内容
 * 
 * @param positionParams 包含布局参数的对象，如列数和最大行数
 * @param items 布局项数组，每个布局项都有自己的位置和尺寸属性
 * @returns 返回调整后的位置和大小的布局项数组
 */
export function shiftInside(positionParams: PositionParams, items: LayoutItem[]): LayoutItem[] {
  // 计算所有布局项的最小和最大边界
  const minX = Math.min(...items.map((item) => item.x));
  const minY = Math.min(...items.map((item) => item.y));
  const maxX = Math.max(...items.map((item) => item.x + item.w));
  const maxY = Math.max(...items.map((item) => item.y + item.h));

  // 初始化横向和纵向的位移
  let deltaX: number = 0;
  let deltaY: number = 0;

  // 根据布局项的最小和最大边界，计算需要的横向位移
  if (minX < 0) {
    // 如果最小X坐标小于0，则需要向右移动
    deltaX = -minX;
  } else if (maxX > positionParams.cols) {
    // 如果最大X坐标大于列数，则需要向左移动，同时保证最小X坐标不小于0
    deltaX = Math.max(positionParams.cols - maxX, -minX);
  }

  // 根据布局项的最小和最大边界，计算需要的纵向位移
  if (minY < 0) {
    // 如果最小Y坐标小于0，则需要向下移动
    deltaY = -minY;
  } else if (maxY > positionParams.maxRows) {
    // 如果最大Y坐标大于最大行数，则需要向上移动，同时保证最小Y坐标不小于0
    deltaY = Math.max(positionParams.maxRows - maxY, -minY);
  }

  // 根据计算出的位移，调整每个布局项的位置和大小
  const newItems: LayoutItem[] = items.map((item) => {
    let newItem = {
      ...item,
      x: item.x + deltaX,
      y: item.y + deltaY,
    };

    // 检查调整后的位置，如果超出范围，则进一步调整
    if (newItem.x + newItem.w > positionParams.cols) {
      newItem.x = Math.max(0, positionParams.cols - newItem.w);
      if (newItem.x + newItem.w > positionParams.cols) {
        newItem.w = positionParams.cols - newItem.x;
      }
    }
    if (newItem.y + newItem.h > positionParams.maxRows) {
      newItem.y = Math.max(0, positionParams.maxRows - newItem.h);
      if (newItem.y + newItem.h > positionParams.maxRows) {
        newItem.h = positionParams.maxRows - newItem.y;
      }
    }
    return newItem;
  });

  // 返回调整后的位置和大小的布局项数组
  return newItems;
}

/**
 * 将超出边界的元素进行缩窄，最小宽度为 1。
 * 
 * @param item 元素
 * @param start 沿 X 轴的最小坐标，左闭右开
 * @param end 沿 X 轴的最大坐标，左闭右开
 */
export function narrow(item: LayoutItem, start: number = 0, end: number): LayoutItem {
  let { x, w } = item;
  if (x < start) {
    w = Math.max(1, x + w - start);
    x = start;
  }
  if (x + w > end) {
    w = Math.max(1, end - x);
    x = end - w;
  }
  return { ...item, x, w };
}

/**
 * 将超出边界的多个元素进行缩窄，最小宽度为 1。
 * 
 * @param items 元素列表
 * @param cols 最大宽度
 */
export function narrowItems(items: LayoutItem[], cols: number): LayoutItem[] {
  // log.debug("start narrowItems. items: ", items);
  // loop from the left
  items = _.sortBy(items, (item) => item.x);
  let newItems: LayoutItem[] = [];
  items.forEach((item) => {
    const start = Math.max(
      0,
      ...newItems
        .filter(
          (newItem) =>
            newItem.y < item.y + item.h &&
            item.y < newItem.y + newItem.h &&
            item.x < newItem.x + newItem.w
        )
        .map((newItem) => newItem.x + newItem.w)
    );
    const newItem = narrow(item, start, Infinity);
    newItems.push(newItem);
  });

  // loop from the right
  items = _.sortBy(newItems, (item) => -(item.x + item.w));
  newItems = [];
  items.forEach((item) => {
    const end = Math.min(
      cols,
      ...newItems
        .filter(
          (newItem) =>
            newItem.y < item.y + item.h &&
            item.y < newItem.y + newItem.h &&
            newItem.x < item.x + item.w
        )
        .map((newItem) => newItem.x)
    );
    const newItem = narrow(item, 0, end);
    // log.debug("item: ", item, " newItems: ", newItems, " end: ", end, " newItem: ", newItem);
    newItems.push(newItem);
  });

  // log.debug("finish narrowItems. items: ", newItems);
  return newItems;
}

/**
 * 检查布局项是否有效
 * 
 * @param item LayoutItem 类型的对象，表示一个布局项
 * @param positionParams PositionParams 类型的对象，包含布局的列数和最大行数
 * @returns 返回一个布尔值，表示布局项是否有效
 * 
 * 布局项被视作有效的条件如下：
 * - x 坐标和 y 坐标必须大于等于 0
 * - 宽度 w 和高度 h 必须大于 0
 * - 布局项的右边界（x + w）不能超过布局的总列数（positionParams.cols）
 * - 布局项的下边界（y + h）不能超过布局的最大行数（positionParams.maxRows）
 * 
 * 这些条件确保了布局项在可视化布局中是有效且合理的，避免了负坐标或超出边界的情况
 */
export function isValidLayoutItem(item: LayoutItem, positionParams: PositionParams): boolean {
  return (
    item.x >= 0 &&
    item.y >= 0 &&
    item.w > 0 &&
    item.h > 0 &&
    item.x + item.w <= positionParams.cols &&
    item.y + item.h <= positionParams.maxRows
  );
}

/**
 * 判断两个矩形是否相交
 * 
 * 此函数通过检查两个矩形的位置和大小来判断它们是否相交
 * 矩形由其左上角坐标（x, y）以及宽度（w）和高度（h）定义
 * 
 * @param item1 第一个矩形，包含x, y, w, h属性
 * @param item2 第二个矩形，包含x, y, w, h属性
 * @returns 如果两个矩形相交，返回true；否则返回false
 * 
 * 注意：相交的判断是通过检查矩形A的任何部分是否与矩形B的任何部分重叠来实现的
 * 这种方法也称为边界矩形交集测试
 */
function intersect(
  item1: { x: number; y: number; w: number; h: number },
  item2: { x: number; y: number; w: number; h: number }
): boolean {
  return (
    item1.x < item2.x + item2.w &&
    item1.y < item2.y + item2.h &&
    item2.x < item1.x + item1.w &&
    item2.y < item1.y + item1.h
  );
}

/**
 * 检查两个布局项数组是否有交集
 * 
 * 此函数通过遍历两个布局项数组中的每一项，并使用交集检测函数`intersect`来判断两项是否相交
 * 如果找到任何一对相交的项，函数将返回`true`，表示这两个数组存在交集；否则返回`false`
 * 
 * @param items1 第一个布局项数组
 * @param items2 第二个布局项数组
 * @returns 返回一个布尔值，表示两个数组是否存在交集
 */
export function hasIntersections(items1: LayoutItem[], items2: LayoutItem[]) {
  // 遍历第一个数组中的每一项
  for (const item1 of items1) {
    // 遍历第二个数组中的每一项
    for (const item2 of items2) {
      // 如果两项相交，则返回true
      if (intersect(item1, item2)) return true;
    }
  }
  // 如果没有相交的项，则返回false
  return false;
}

const IN_CANVAS_COUNT = "inCanvasCount";
export function getInCanvasCount() {
  return draggingUtils.getData<number>(IN_CANVAS_COUNT) ?? 0;
}

export function updateInCanvasCount(delta: number) {
  const inCanvas = getInCanvasCount();
  const newInCanvas = Math.max(-1, Math.min(1, inCanvas + delta));
  if (inCanvas !== newInCanvas) {
    // log.debug( "updateInCanvas. delta: ", delta, "inCanvas: ", inCanvas, "newInCanvas: ", newInCanvas);
    draggingUtils.setData(IN_CANVAS_COUNT, newInCanvas);
  }
}

/**
 * 根据滚动条的位置调整元素的滚动状态
 * 
 * 该函数通过计算滚动条距离顶部和底部的比例，判断是否需要触发滚动动画，
 * 并根据滚动条的具体位置调整元素的滚动方向和速度
 * 
 * @param element 要操作的HTMLDivElement，即滚动容器
 * @param topRatio 滚动条距离顶部的比例，用于判断滚动条的位置
 */
export function edgeScroll(element: HTMLDivElement, topRatio: number) {
  // 定义触发滚动动画的阈值比例
  const THR = 0.15;

  // 判断滚动条是否接近顶部
  if (topRatio < THR) {
    // 调用滚动函数，参数分别为元素、滚动速度和滚动方向（-1表示向上滚动）
    doScroll(element, (THR - topRatio) / THR, -1);
  }

  // 计算滚动条距离底部的比例
  const bottomRatio = 1 - topRatio;

  // 判断滚动条是否接近底部
  if (bottomRatio < THR) {
    // 调用滚动函数，参数分别为元素、滚动速度和滚动方向（1表示向下滚动）
    doScroll(element, (THR - bottomRatio) / THR, 1);
  }
}

/**
 * 根据给定的滚动比例和步长调整元素的滚动位置
 * 
 * @param element 要操作的HTML元素，应为一个HTMLDivElement
 * @param ratio 滚动比例，用于调整滚动的速度
 * @param step 滚动的步长，正数表示向下滚动，负数表示向上滚动
 */
function doScroll(element: HTMLDivElement, ratio: number, step: number) {
  // 计算新的滚动位置，并应用到元素上
  element.scrollTop = element.scrollTop + (2 + ratio * 4) * step;
}

/**
 * 计算一组布局项目的总高度
 * 
 * 该函数通过找出一组布局项目中的最上边和最下边的位置，计算出总的布局高度
 * 总高度为最下边的位置减去最上边的位置，确保计算结果为非负数
 * 
 * @param items 布局项目的数组，每个项目都有y坐标和高度h
 * @returns 返回布局项目的总高度
 */
export function calcTotalHeight(items: LayoutItem[]) {
  // 找出所有项目的最小y坐标，作为最上边的位置
  const top = Math.min(...items.map((item) => item.y));

  // 找出所有项目的最大y坐标加上高度，作为最下边的位置
  const bottom = Math.max(...items.map((item) => item.y + item.h));

  // 计算并返回总高度，确保结果不为负数
  return Math.max(0, bottom - top);
}

export type FlowLayoutItem = {
  i: string;
};
export type FlowLayout = Array<FlowLayoutItem>;

/**
 * 排序布局数据，并重新编序
 * @param layout 布局对象，其元素包含z属性
 * @returns 返回包含更新后的布局和最大z值的对象
 */
export function ensureUniqueZArray(layout: Layout | { [x: string]: layoutsNodeItem }, key: string = 'i') {
  let newArry = _.orderBy(layout, ['z'], ['asc'])
  Array.from(newArry.values()).forEach((item, index) => { item.z = index + 1; })
  return newArry
}

export function ensureUniqueZObject(layout: Layout | { [x: string]: layoutsNodeItem }, key: string = 'i') {
  return _.keyBy(ensureUniqueZArray(layout, key), key)
}

// 准确比较函数是否相同，速度太慢，仅测试用
const compareFuntionTEST = (a: any, b: any): boolean => {
  // 转为字符串
  let strA: string = a.toString();
  let strB: string = b.toString();

  // 格式化
  function fromat(str: string): string {
    str = str.replaceAll(/\/\/[^\n]+\n/g, '\n'); // 去掉注释 //
    str = str.replaceAll(/\/\*[\S\s]+\*\//g, '\n'); // 去掉注释 /**/
    str = str.replaceAll(/;[\s]+\n/g, ';'); // 去掉分号后的空字符
    str = str.replaceAll(/[ ]+/g, ' '); // 将多空格缩短为一个空格
    str = str.replaceAll(/[\n\r\t]+/g, ''); // 将多换行多制表缩短为一个换行
    return str;
  }
  strA = fromat(strA);
  strB = fromat(strB);

  // 提取形参
  function getArgs(str: string): string[] {
    const args: string[] = str
      .slice(str.indexOf('(') + 1, str.indexOf(')'))
      .replaceAll(' ', '')
      .split(',');
    return args;
  }
  const argsA: string[] = getArgs(strA);
  const argsB: string[] = getArgs(strB);

  // strB替换成和strA相同的形参
  function setSameArgs(strB: string, argsA: string[], argsB: string[]): string {
    for (let i = 0; i < argsB.length; i++) {
      // 不需要替换的场景
      // this.a || this.['a'] || this.["a"]
      // 需要替换的场景
      // ...a || this.[a] || a[0] || a.length
      strB = strB.replaceAll(new RegExp(`([^.'"\\w]|[.]{3})${argsB[i]}\\W`, 'g'), s => {
        return s.replaceAll(/[0-9a-zA-Z_]+/g, argsA[i]);
      });
    }
    return strB;
  }
  strB = setSameArgs(strB, argsA, argsB);

  return strA === strB;
};
