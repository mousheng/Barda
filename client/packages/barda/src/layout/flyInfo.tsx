import { PositionParams } from "./calculateUtils";
import { ExtraLayout, Layout } from "./utils";

// 拖动初始信息
export const FLY_START_INFO = "flyStartInfo";
// 拖动经过信息
export const FLY_OVER_INFO = "flyOverInfo";
// 拖动切换函数
export const FLY_SWITCH_FN = "flySwitchFn"
// 添加新组件标识
export const ADD_NEW_COMP_KEY = "__addNewComp__";
// 开始调整大小
export const RESIZEING_START_LAYOUT = "resizeingStartLayout";
// 当前拖动容器的ref
export const CURRENT_CONTAINER_REF = "currentContainerRef";
// drop事件处置标识
export const DROP_EVENT_PROCESSED = "dropEventProcessed";

export type FlyStartInfo = {
  readonly originalLayout: Layout;  // 初始布局
  readonly switchedLayout: Layout;  // 拖拽到其他容器后的布局
  readonly flyItemKeys: string[];   // 需要移动的所有item的key
  readonly flyItemLayouts: Layout;
  readonly currentItemID: string;   // 当前拖拽的item的id
  readonly colWidth: number;  // 列宽
  readonly containerRef: React.RefObject<HTMLDivElement>;
  readonly flyStartPosition: { x: number, y: number };  // 拖拽开始位置
  readonly flyExtraLayout: ExtraLayout;
  readonly flyLeftAdjItems: Record<string, string[]>;
  readonly flyPositionParams: PositionParams;
  readonly flyStartRecoverFn: () => void;
};

export type FlyOverInfo = {
  readonly layoutRef: React.RefObject<HTMLDivElement>;
  readonly switchFn: (flyOverInfo?: FlyOverInfo) => void;
  readonly dropFn: () => void;
  readonly innerHeight: number;
};

export type FlySwitchFnType = () => void;
