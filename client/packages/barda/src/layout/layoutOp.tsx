import { LayoutItem } from "./utils";

export enum LayoutOpTypes {
  CHANGE_ITEM = "CHANGE_ITEM",
  STICKY_ITEM = "STICKY_ITEM",
  HIDE_ITEM = "HIDE_ITEM",
  DELETE_ITEM = "DELETE_ITEM",
  RENAME_ITEM = "RENAME_ITEM",
  FLAG_ITEM = "FLAG_ITEM",
}

export interface CommonLayoutOp {
  type: LayoutOpTypes;
}

export interface ChangeItemOp extends CommonLayoutOp {
  type: LayoutOpTypes.CHANGE_ITEM;
  key: string;
  item: Partial<LayoutItem>;
}

export interface StickyItemOp extends CommonLayoutOp {
  type: LayoutOpTypes.STICKY_ITEM;
  key: string;
  item: Partial<LayoutItem>;
}

export interface HideItemOp extends CommonLayoutOp {
  type: LayoutOpTypes.HIDE_ITEM;
  key: string;
}

export interface DeleteItemOp extends CommonLayoutOp {
  type: LayoutOpTypes.DELETE_ITEM;
  key: string;
}

export interface RenameItemOp extends CommonLayoutOp {
  type: LayoutOpTypes.RENAME_ITEM;
  sourceKey: string;
  targetKey: string;
}

// 用于标记当前布局需要保存的操作,timestamp用于存储时间戳来判断是否已保存
export interface flagItemop extends CommonLayoutOp {
  type: LayoutOpTypes.FLAG_ITEM;
  timestamp: number;
}

export type LayoutOp = ChangeItemOp | StickyItemOp | HideItemOp | DeleteItemOp | RenameItemOp | flagItemop;

export function changeItemOp(key: string, item: Partial<LayoutItem>): ChangeItemOp {
  return { type: LayoutOpTypes.CHANGE_ITEM, key, item };
}

export function stickyItemOp(key: string, item: Partial<LayoutItem>): StickyItemOp {
  return { type: LayoutOpTypes.STICKY_ITEM, key, item };
}

export function hideItemOp(key: string): HideItemOp {
  return { type: LayoutOpTypes.HIDE_ITEM, key };
}

export function deleteItemOp(key: string): DeleteItemOp {
  return { type: LayoutOpTypes.DELETE_ITEM, key };
}

export function renameItemOp(sourceKey: string, targetKey: string): RenameItemOp {
  return { type: LayoutOpTypes.RENAME_ITEM, sourceKey, targetKey };
}

export function flagItemOp(timestamp: number): flagItemop {
  return { type: LayoutOpTypes.FLAG_ITEM, timestamp };
}
