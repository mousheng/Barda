import _ from "lodash";
import { memoizeN } from "util/memoize/memoizeN";
import { changeItemOp, LayoutOp, LayoutOpTypes, stickyItemOp } from "./layoutOp";
import {
  cascade,
  changeStickyItem,
  ExtraLayout,
  myGetStickyItemMap,
  Layout,
  modifyLayout,
  withLayoutItem,
} from "./utils";
import { LayoutModeType } from "./gridLayoutPropTypes";

export type LayoutOps = LayoutOp[];
export namespace layoutOpUtils {
  // try to reduce two adjacent ops to one
  function mergeOp(op1: LayoutOp, op2: LayoutOp): LayoutOp | undefined {
    if (op2.type === LayoutOpTypes.CHANGE_ITEM) {
      if (op1.type === LayoutOpTypes.CHANGE_ITEM && op1.key === op2.key) {
        return changeItemOp(op1.key, { ...op1.item, ...op2.item });
      }
    }
    if (op2.type === LayoutOpTypes.DELETE_ITEM) {
      if (
        (op1.type === LayoutOpTypes.CHANGE_ITEM ||
          op1.type === LayoutOpTypes.DELETE_ITEM ||
          op1.type === LayoutOpTypes.HIDE_ITEM) &&
        op1.key === op2.key
      ) {
        return op2;
      }
    }
    if (op2.type === LayoutOpTypes.STICKY_ITEM) {
      if (op1.type === LayoutOpTypes.STICKY_ITEM && op1.key === op2.key) {
        return stickyItemOp(op1.key, { ...op1.item, ...op2.item });
      }
    }
    return undefined;
  }

  function shouldSkipPrev(prevOp: LayoutOp, op: LayoutOp): boolean {
    if (
      op.type === LayoutOpTypes.CHANGE_ITEM ||
      op.type === LayoutOpTypes.DELETE_ITEM ||
      op.type === LayoutOpTypes.HIDE_ITEM
    ) {
      if (
        (prevOp.type === LayoutOpTypes.CHANGE_ITEM ||
          prevOp.type === LayoutOpTypes.DELETE_ITEM ||
          prevOp.type === LayoutOpTypes.HIDE_ITEM) &&
        prevOp.key !== op.key
      ) {
        return false;
      }
    }
    return true;
  }

  export function batchPush(ops: LayoutOps | undefined, newOps: LayoutOps): LayoutOps {
    let finalOps = ops ?? [];
    newOps.forEach((op) => {
      finalOps = push(finalOps, op);
    });
    return finalOps;
  }

  export function push(ops: LayoutOps | undefined, op: LayoutOp): LayoutOps {
    if (!ops) {
      return [op];
    }

    let newOps: LayoutOps = [];
    // check whether to reduce in the reversed order
    for (let i = ops.length - 1; i >= 0; --i) {
      const prevOp = ops[i];
      const newOp = mergeOp(prevOp, op);
      // can be merged or not
      if (newOp) {
        op = newOp;
      } else {
        newOps.push(prevOp);
        if (shouldSkipPrev(prevOp, op)) {
          newOps = newOps.concat(ops.slice(0, i).reverse());
          break;
        }
      }
    }
    newOps.reverse();
    newOps.push(op);
    return newOps;
  }
}

function reduce(layout: Layout, op: LayoutOp, stickyItemMap?: Record<string, Set<string>>): Layout {
  if (op.type !== LayoutOpTypes.CHANGE_ITEM && !(_.get(op, "key") || _.get(op, "sourceKey"))) {
    return layout;
  }
  let newLayout = layout;
  switch (op.type) {
    case LayoutOpTypes.CHANGE_ITEM: {
      const item = layout[op.key];
      newLayout = modifyLayout(layout, { ...item, ...op.item });
      break;
    }
    case LayoutOpTypes.STICKY_ITEM: {
      const item = layout[op.key];
      newLayout = changeStickyItem(layout, { ...item, ...op.item }, stickyItemMap);
      break;
    }
    case LayoutOpTypes.HIDE_ITEM: {
      newLayout = withLayoutItem(layout, op.key, (item) => ({
        ...item,
        hide: true,
      }))[0];
      break;
    }
    case LayoutOpTypes.DELETE_ITEM: {
      newLayout = _.omit(layout, op.key);
      break;
    }
    case LayoutOpTypes.RENAME_ITEM: {
      const item = layout[op.sourceKey];
      if (_.isNil(item)) break;
      newLayout = {
        ..._.omit(layout, [op.sourceKey]),
        [op.targetKey]: { ...item, i: op.targetKey },
      };
      break;
    }
  }
  return newLayout;
}

/**
 * 从一个复合状态推断最终布局。
 * - sticky: 预先计算 `stickyItemMap`，表示项目之间的粘性关系。
 * - hidden: 对隐藏的项目设置 h=0，并在完成布局后重置 h。
 *
 * @param layout 保存的源布局
 * @param extraLayout 为项目提供隐藏和选中属性的额外信息
 * @param changedHs 记录自动高度信息
 * @param ops 其他操作
 * @returns 最终布局
 */
export let getUILayout = (
  layout: Layout,
  extraLayout: ExtraLayout | undefined,
  changedHs: Record<string, number> | undefined,
  ops: LayoutOps | undefined,
  layoutMode: LayoutModeType = "grid",
  setHiddenCompHeightZero: boolean = false,
): Layout => {
  // console.log("getUILayout. layout: ", { layout, extraLayout, changedHs, ops, stickyItemMap });
  const inputLayout = layout;
  const stickyItemMap = myGetStickyItemMap(layout);
  const hiddenItemHeight = _.fromPairs(
    _.toPairs(extraLayout)
      .filter(([, extraItem]) => extraItem.hidden && !extraItem.isSelected)
      .map(([i]) => [i, layout[i]?.h ?? 0])
  );
  const realOps = [
    ..._.toPairs(changedHs)
      .filter(([i, h]) => layout.hasOwnProperty(i) && layout[i].h !== h)
      .map(([i, h]) => (layout[i].h > h ? stickyItemOp(i, { h }) : changeItemOp(i, { h }))),
    ...Object.keys(hiddenItemHeight).map((i) => stickyItemOp(i, { h: 0 })),
    ...(ops ?? []),
  ];
  realOps.forEach((op) => {
    layout = _.isEmpty(layout) && op.type !== LayoutOpTypes.CHANGE_ITEM ? {} : reduce(layout, op, stickyItemMap);
  });
  // 是否处理碰撞问题
  if (layoutMode === "grid")
    layout = cascade(layout);
  if (!setHiddenCompHeightZero) {
    const recoverHiddenOps = _.toPairs(hiddenItemHeight).map(([i, h]) => changeItemOp(i, { h }));
    recoverHiddenOps.forEach((op) => {
      layout = reduce(layout, op, stickyItemMap);
    });
  }
  const stabilized: Layout = {};
  for (const key of Object.keys(layout)) {
    const orig = inputLayout[key];
    const curr = layout[key];
    if (
      orig &&
      orig.x === curr.x &&
      orig.y === curr.y &&
      orig.w === curr.w &&
      orig.h === curr.h &&
      orig.minW === curr.minW &&
      orig.minH === curr.minH &&
      orig.maxW === curr.maxW &&
      orig.maxH === curr.maxH &&
      orig.isDragging === curr.isDragging &&
      orig.placeholder === curr.placeholder &&
      orig.hide === curr.hide &&
      orig.static === curr.static &&
      orig.delayCollision === curr.delayCollision
    ) {
      stabilized[key] = orig;
    } else {
      stabilized[key] = curr;
    }
  }
  // log.log("getUILayout. finalLayout: ", layout);
  return stabilized;
};

getUILayout = memoizeN(getUILayout, { comparators: _.isEqual, order: [3, 5, 1, 0, 2, 4] });
