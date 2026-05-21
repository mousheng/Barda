import { StandardBoxMargin } from "@barda/comps/controls/styleControlConstants";
import { useEditorStore } from "@barda/comps/editorStore";
import { UICompType } from "@barda/comps/uiCompRegistry";
import { ModulePrimaryColor, PrimaryColor } from "@barda/constants/style";
import clsx from "clsx";
import { colord } from "colord";
import _ from "lodash";
// import log from "loglevel";
import { isDirectionKey, isFilterInputTarget, modKeyPressed } from "@barda/util/keyUtils";
import React, { Children, DragEvent, DragEventHandler, useCallback, useContext, useEffect, useLayoutEffect, useMemo, useRef, useState } from "react";
import { useResizeDetector } from "react-resize-detector";
import { useTimeoutFn } from 'react-use';
import styled from "styled-components";
import { calcGridColWidth, calcGridItemPosition, calcGridItemSizePx, calcWH, calcXY, genPositionParams, PositionParams } from "./calculateUtils";
import { draggingUtils } from "./draggingUtils";
import { ADD_NEW_COMP_KEY, CURRENT_CONTAINER_REF, DROP_EVENT_PROCESSED, FLY_OVER_INFO, FLY_START_INFO, FLY_SWITCH_FN, FlyOverInfo, FlyStartInfo, FlySwitchFnType, RESIZEING_START_LAYOUT } from "./flyInfo";
import { GridItem } from "./gridItem";
import { GridLayoutProps } from "./gridLayoutPropTypes";
import { GridLines } from "./gridLines";
import { changeItemOp, deleteItemOp, flagItemOp, flagItemop, LayoutOpTypes } from "./layoutOp";
import { getUILayout, LayoutOps, layoutOpUtils } from "./layoutOpUtils";
import { bottom, calcLeftAdjacentItems, calcOffset, canResizeBottom, canResizeRight, collides, edgeScroll, ExtraItem, ExtraLayout, getItemResizeHandles, GridResizeEvent, isItemDraggable, isItemResizable, isValidLayoutItem, Layout, LayoutItem, narrow, narrowItems, shiftInside, synchronizeLayoutWithChildren } from "./utils";
import { ScrollBar } from "components/ScrollBar";

// log.setLevel(log.levels.DEBUG)
const Wapper = styled.div`
    height: 100%;
`
const LayoutContainer = styled.div<{
    $bgColor?: string;
    $autoHeight?: boolean;
    $overflow?: string;
    $radius?: string;
}>`
    border-radius: ${(props) => props.$radius ?? "4px"};
    background-color: ${(props) => props.$bgColor ?? "#f5f5f6"};
  
    overflow: hidden;
    ${(props) =>
        props.$autoHeight &&
        `&::-webkit-scrollbar {
      display: none;
    }`}
  `;

const DragPlaceHolder = styled.div<{ $compType: UICompType, $itemMargin?: StandardBoxMargin }>`
  height: calc(100% - ${props => props.$itemMargin?.[0] ?? 2.5}px - ${props => props.$itemMargin?.[2] ?? 2.5}px);
  width: calc(100% - ${props => props.$itemMargin?.[3] ?? 6.5}px - ${props => props.$itemMargin?.[1] ?? 6.5}px);
  background-color: ${(props) => props.$compType === "module" ? ModulePrimaryColor : PrimaryColor} !important;
  top: ${props => props.$itemMargin?.[0] ?? 2.5}px;
  left: ${props => props.$itemMargin?.[3] ?? 6.5}px;
  opacity: ${props => props.$itemMargin === undefined ? 0.2 : 1};
  position: absolute;
  backdrop-filter: blur(18px);
  z-index: 10;
`;

const LAYOUT_CLASS_NAME = "react-grid-layout";

type GridItemChildCache = {
    baseChild?: React.ReactElement;
    isPlaceholder: boolean;
    isDragging?: boolean;
    isSelected?: boolean;
    margin?: StandardBoxMargin;
    compType?: UICompType;
    child: React.ReactElement;
};

/**
 * 使用函数组件实现网格布局
 * 拖动事件顺序：
 * 1. dragstart    （拖动源）
 * 2. drag         （拖动源）—— 持续触发
 * 3. dragenter    （放置目标）
 * 4. dragover     （放置目标）—— 持续触发
 * 5. dragleave    （放置目标）
 * 6. drop         （放置目标）
 * 7. dragend      （拖动源）
 * @param props 
 * @returns 
 */
export const NewGridLayout = (props: GridLayoutProps) => {
    const { hintPlaceholder } = props;
    // 用于组件刷新
    const [refreshCanvasCount, setRefreshCanvasCount] = useState<number>(0)
    // 最终呈现的布局
    const [layouts, setLayouts] = useState<Layout>({});
    // 布局操作
    const [layoutOPS, setLayoutOPS] = useState<LayoutOps>([]);
    // 保存最后保存布局的时间戳
    const layoutSaveTimeStamps = useRef<number>(0);
    // 拖拽时保存的初始布局
    const originalLayoutRef = useRef<Layout>({});
    // 用于判断是否在画布内的计数
    const inCanvasCountRef = useRef(0);
    // 保存拖拽时组件的当前位置，减少onDrag的触发次数
    const draggingItemPosition = useRef({ x: 0, y: 0 });
    // 保存dragOver时组件的当前位置，减少onDragOver的触发次数
    const dragOverItemPosition = useRef({ x: 0, y: 0 });
    // 保存布局中最后一个组件的y坐标
    const layoutBottomRef = useRef(0);
    // 删除占位及隐藏拖动组件标识
    const deletePlaceholderAndHideDraggingCompRef = useRef(false);
    const { height: innerHeight, ref } = useResizeDetector();
    const { height: WapperHeight, ref:wapperRef } = useResizeDetector();
    // 临时保存冻结的布局
    const FrozenLayout = useRef({})
    // 临时保存冻结的容器名
    const frozenCompNameRef = useRef<string>("");
    // 保存拖拽时组件的当前大小，减少onResize的触发次数
    const resizeWH = useRef({ w: 0, h: 0 });
    // 组件高度映射
    const compsHeightMap = useRef<Record<string, number>>(_.mapValues(props.layout, 'h'))
    const mergedClassName = useMemo(() => clsx(LAYOUT_CLASS_NAME, props.className), [props.className]);
    const mounted = useMemo(() => _.isNumber(props.gridWidth) && props.gridWidth > 0, [props.gridWidth]);
    const positionParams = useMemo(() => genPositionParams(props), [props.cols, props.margin, props.maxRows, props.rowHeight, props.gridWidth, props.containerPadding]);
    const contrastBgColor = useMemo(() => colord(props.bgColor ?? "#ffffff").invert().alpha(0.05).toHex(), [props.bgColor]);
    const selectedKeys = useMemo(() => Object.keys(_.pickBy(props.extraLayout, (extraItem) => !!extraItem.isSelected)), [props.extraLayout])
    const scrollHeightRef = useRef(0);
    const gridItemChildCacheRef = useRef<Record<string, GridItemChildCache>>({});
    const showNameProps = useMemo(() => ({
        top: props.showName?.top ?? 0,
        bottom: (props.showName?.bottom ?? 0) + scrollHeightRef.current,
    }), [props.showName?.top, props.showName?.bottom, scrollHeightRef.current]);

    // 手动刷新画布
    const refreshCanvas = () => {
        setRefreshCanvasCount(pre => (pre + 1) % 100)
    }

    useEffect(() => {
        // 当extraLayout变化时，移除stateChangedHs中autoHeight为false的项
        if (props.extraLayout) {
            const filtered = Object.fromEntries(
                Object.entries(compsHeightMap.current).filter(([key]) => props.extraLayout?.[key]?.autoHeight !== false)
            );
            if (Object.keys(filtered).length !== Object.keys(compsHeightMap).length) {
                compsHeightMap.current = filtered;
                refreshCanvas();
            }
        }
    }, [props.extraLayout]);

    function saveResize() {
        const originalLayouts = draggingUtils.getData<Layout>(RESIZEING_START_LAYOUT);
        if (!originalLayouts) return;
        const layout = getUILayout(originalLayouts, props.extraLayout, compsHeightMap.current, layoutOPS, props.layoutMode, false);
        // 清除拖拽工具
        draggingUtils.clearData();
        props.onLayoutChange?.(layout);
    }

    function setFrozenLayouts() {
        setLayouts(FrozenLayout.current)
    }

    const [isReadySaveResize, cancelSaveResize, resetSaveResize] = useTimeoutFn(saveResize, 100);
    const [isShowFrozenLayout, cancelFrozeLayout, resetDelaySetLayouts] = useTimeoutFn(setFrozenLayouts, 100);

    useEffect(() => {
        cancelSaveResize();
        cancelFrozeLayout();
    }, [])

    // 布局操作包装，用于日志打印
    const SetLayoutOPSWrapper = (value: LayoutOps, flag: string) => {
        // log.debug(flag, ' 操作列表:', value);
        setLayoutOPS(value)
    }


    const setLayoutsWrapper = (value: Layout | ((prev: Layout) => Layout), flag: string) => {
        if (typeof value === "function") {
            setLayouts(prev => {
                const newValue = value(prev);
                // log.debug(flag, "新值:", newValue, "前值:", prev);
                layoutBottomRef.current = bottom(newValue)
                return newValue;
            })
        } else {
            // log.debug(flag, "新值:", value, "前值:", layouts);
            layoutBottomRef.current = bottom(value)
            setLayouts(value)
        }
    }

    /** 封装获取布局函数 */
    const getUILayouts = useCallback((layout: Layout, ops?: LayoutOps, setHiddenCompHeightZero: boolean = false) => {
        return getUILayout(layout, props.extraLayout, compsHeightMap.current, ops, props.layoutMode, setHiddenCompHeightZero);
    }, [props.extraLayout, props.layoutMode])

    // 缓存布局容器的高度
    useLayoutEffect(() => {
        scrollHeightRef.current = ref.current?.scrollHeight ?? 0;
    }, [ref.current])

    useEffect(() => {
        const newLayouts = synchronizeLayoutWithChildren(
            props.layout as Layout,
            compsHeightMap.current,
            props.children as NonNullable<typeof props.children>,
            props.cols as NonNullable<typeof props.cols>,
        )
        SetLayoutOPSWrapper([], "同步-清除操作")
        setLayoutsWrapper(newLayouts, "同步-设置布局")
        if (!_.isEqual(newLayouts, layouts)) {
            props.onLayoutChange?.(newLayouts);
        }
    }, [Children.count(props.children), props.layout])

    const handleFrozen = (layout: Layout) => {
        // 检查是否需要冻结
        const frozenCompName = checkContainerMove(originalLayoutRef.current, layout);
        if (frozenCompName === "") {
            frozenCompNameRef.current = "";
            cancelFrozeLayout();
            return false;
        } else if (frozenCompName !== frozenCompNameRef.current) {
            FrozenLayout.current = layout;
            frozenCompNameRef.current = frozenCompName;
            cancelFrozeLayout();
            resetDelaySetLayouts()
            return true;
        } else if (isShowFrozenLayout() === false) {
            FrozenLayout.current = layout;
            cancelFrozeLayout();
            resetDelaySetLayouts()
            return true;
        }
    }

    // 调整组件大小的副作用
    useLayoutEffect(() => {
        const isDragging = draggingUtils.isDragging();
        const layout = getUILayouts(isDragging ? props.layout : layouts, layoutOPS);
        // 处理是否冻结容器方便拖入
        if (handleFrozen(layout)) return;
        setLayoutsWrapper(layout, "副作用-设置布局");
        const lastOps = layoutOPS.at(-1) as flagItemop;
        if (!_.isEmpty(lastOps) && lastOps?.type === LayoutOpTypes.FLAG_ITEM && lastOps.timestamp !== layoutSaveTimeStamps.current) {
            layoutSaveTimeStamps.current = lastOps.timestamp;
            const flyStartInfo = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO);
            if (flyStartInfo && flyStartInfo?.containerRef !== ref.current) {
                // 调用onFlyDrop处理拖入的组件
                // props.onFlyDrop?.(layout, _.pick(layout, flyStartInfo.flyItemKeys));
                inCanvasCountRef.current = 0;
            } else {
                props.onLayoutChange?.(layout);
                SetLayoutOPSWrapper([], '副作用-清除操作')
            }
        }
    }, [layoutOPS, refreshCanvasCount])

    const checkContainerMove = (prevLayout: Layout, currentLayout: Layout): string => {
        return Object.entries(prevLayout)
            .filter(([key, prevItem]) => {
                // 剔除不需要比较的 key
                if (selectedKeys.includes(key) || key === ADD_NEW_COMP_KEY) return false;
                return prevItem.delayCollision === true;
            })
            .find(([key, prevItem]) => {
                const currItem = currentLayout[key];
                return currItem && (prevItem.x !== currItem.x || prevItem.y !== currItem.y);
            })?.[0] || "";
    };

    const childrenMap: _.Dictionary<typeof props.children[0]> = useMemo(() => _.fromPairs(
        props.children.filter((child) => child.key).map((child) => [child.key, child])
    ),
        [props.children]
    );

    const gridLinesPosition = useMemo(() => {
        const { containerPadding, rowHeight, containerWidth } = positionParams;
        const [left, top] = positionParams.containerPadding;
        const height = Math.max(layoutBottomRef.current * rowHeight + 1, (innerHeight ?? 0) - containerPadding[1] * 2);
        const width = containerWidth - containerPadding[0] * 2 + 1;
        return { left, top, width, height };
    }, [positionParams])

    const getFinalFlyingItems = (offsetX: number, offsetY: number): LayoutItem[] => {
        const flyStartInfo = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO);
        // 检查是否在原容器中
        const sameContainer = flyStartInfo.containerRef === ref.current;
        const startOffsetX = flyStartInfo.flyStartPosition.x;
        const startOffsetY = flyStartInfo.flyStartPosition.y;
        const deltaX = offsetX - startOffsetX;
        const deltaY = offsetY - startOffsetY;
        const keys = flyStartInfo.flyItemKeys;
        const originalLayout = !sameContainer ? flyStartInfo.flyItemLayouts : originalLayoutRef.current;
        const sourcePositionParams = flyStartInfo.flyPositionParams;
        const positionParams: PositionParams = sameContainer ? flyStartInfo.flyPositionParams : genPositionParams(props);

        const calcedItems = keys.map((key) => {
            const item = originalLayout[key];
            const sourcePosition = calcGridItemPosition(
                sourcePositionParams,
                item.x,
                item.y,
                item.w,
                item.h
            );
            const newPosition = {
                ...sourcePosition,
                left: sourcePosition.left + deltaX,
                top: sourcePosition.top + deltaY,
            };
            let { w, h } = calcWH(positionParams, newPosition.width, newPosition.height, {
                w: false,
                h: true,
            });
            let { x, y } = calcXY(positionParams, newPosition.top, newPosition.left, w, h, {
                x: false,
                y: false,
            });
            const finalItem = {
                ...item,
                x,
                y,
                w,
                h,
                isDragging: true,
                placeholder: true,
            };
            return finalItem;
        });

        // comps will finally have a bit change in size. we should special handle this to remain their neighbor relation.
        const flyLeftAdjItems = flyStartInfo.flyLeftAdjItems;
        const adjOKItemMap: Layout = {};
        calcedItems.forEach((item) => {
            const x = _.max(
                flyLeftAdjItems[item.i]
                    ?.filter((leftItemKey) => !_.isNil(adjOKItemMap[leftItemKey]))
                    ?.map((leftItemKey) => adjOKItemMap[leftItemKey].x + adjOKItemMap[leftItemKey].w)
            );
            !_.isNil(x) && (item.x = x);
            adjOKItemMap[item.i] = item;
        });
        if (props.layoutMode === "free") {
            return Object.values(adjOKItemMap);
        } else {
            // handle the boundary problems for multi-drag
            const narrowedItems = narrowItems(Object.values(adjOKItemMap), positionParams.cols);
            const finalItems = shiftInside(positionParams, narrowedItems);
            return finalItems;
        }


    }

    const getGridItemChild = (
        item: LayoutItem,
        baseChild: React.ReactElement,
        isPlaceholder: boolean,
        itemExtraInfo?: ExtraItem
    ) => {
        const cached = gridItemChildCacheRef.current[item.i];
        const isSelected = !!itemExtraInfo?.isSelected;
        if (
            cached &&
            cached.baseChild === baseChild &&
            cached.isPlaceholder === isPlaceholder &&
            cached.isDragging === item.isDragging &&
            cached.isSelected === isSelected &&
            cached.margin === itemExtraInfo?.margin &&
            cached.compType === itemExtraInfo?.compType
        ) {
            return cached.child;
        }

        const child = (
            <>
                {baseChild}
                {item.i !== "moduleContainer" && item.isDragging && isSelected && (
                    <DragPlaceHolder $itemMargin={itemExtraInfo?.margin!} $compType={itemExtraInfo?.compType} />
                )}
            </>
        );
        gridItemChildCacheRef.current[item.i] = {
            baseChild,
            isPlaceholder,
            isDragging: item.isDragging,
            isSelected,
            margin: itemExtraInfo?.margin,
            compType: itemExtraInfo?.compType,
            child,
        };
        return child;
    };

    const processGridItem = (item: LayoutItem): React.ReactElement | undefined => {
        // console.log("processGridItem", item);
        const draggingExtraLayout = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO)?.flyExtraLayout;
        // const delayItem = this.state.delayItem;
        const itemExtraInfo = props.extraLayout?.[item.i] ?? draggingExtraLayout?.[item.i];
        const isPlaceholder = item.i === ADD_NEW_COMP_KEY || !childrenMap[item.i];
        const child = isPlaceholder ? <DragPlaceHolder key={item.i} $compType={itemExtraInfo?.compType} /> : childrenMap[item.i];
        if (!child) return;
        const onHeightChange = (i: string, h: number): void => {
            if (props?.extraLayout?.[i]?.autoHeight) {
                // 自动高度组件，需要更新布局
                if (isReadySaveResize() === false) resetSaveResize();
                compsHeightMap.current = { ...compsHeightMap.current, [i]: h };
                refreshCanvas()
                return
            }
        };

        const onResizeStart = (i: string, w: number, h: number, arg3: GridResizeEvent) => {
            // 初始化拖拽工具
            draggingUtils.clearData();
            // 检查拖拽项是否存在
            if (!layouts[i]) return;
            // 保存调整大小前的布局
            draggingUtils.setData(RESIZEING_START_LAYOUT, layouts);
            // 调用父组件设置editorState为拖拽状态
            // props.onResizeStart?.(arg3.e);
        }

        const onResize = (i: string, w: number, h: number, arg3: GridResizeEvent) => {
            if (props?.extraLayout?.[i].autoHeight === false && !!compsHeightMap.current[i]) {
                compsHeightMap.current = { ..._.omit(compsHeightMap.current, [i]) };
                refreshCanvas();
                return;
            }
            // 未发生大小变化则不更新
            if (resizeWH.current.w === w && resizeWH.current.h === h) return;
            const droppingItem = { x: arg3.x, y: arg3.y, w, h };
            // 保存当前大小
            resizeWH.current = { w, h }
            // 更新布局操作
            let ops = [changeItemOp(i, { ...droppingItem, isDragging: true })];
            SetLayoutOPSWrapper(ops, "调整大小");
        }

        const onResizeStop = (i: string, w: number, h: number, arg3: GridResizeEvent) => {
            // 调用父组件设置editorState为停止拖拽状态
            props.onResizeStop?.(arg3.e);
            resetSaveResize();
        }

        /**
         * 拖拽事件顺序：1
         * 1. 检查是否在输入框中，如果是则阻止拖拽
         * 2. 处理选中状态
         * 3. 保存初始布局状态
         * 4. 计算拖拽起始位置
         * 5. 保存拖拽相关信息到 draggingUtils 中
         */
        const onDragStart = (i: string, e: DragEvent<HTMLDivElement>, node: HTMLDivElement) => {
            let { transformScale, extraLayout } = props;

            // 输入框输入时，不相应拖拽
            const activeElement = document.activeElement;
            if (activeElement instanceof HTMLInputElement || activeElement instanceof HTMLTextAreaElement) {
                e.preventDefault();
                return;
            }

            let keys = selectedKeys;
            // 如果拖拽的组件没有选中，则选中它
            if (!keys.includes(i)) {
                keys = [i];
                extraLayout = _.mapValues(extraLayout, (extraItem, key) => {
                    if (key === i && !extraItem.isSelected) {
                        return { ...extraItem, isSelected: true };
                    } else if (key !== i && extraItem.isSelected) {
                        return { ...extraItem, isSelected: false };
                    }
                    return extraItem;
                });
            }
            deletePlaceholderAndHideDraggingCompRef.current = false;
            draggingUtils.setData(DROP_EVENT_PROCESSED, false);
            draggingUtils.setData(CURRENT_CONTAINER_REF, ref.current);
            // 初始布局
            originalLayoutRef.current = { ...layouts };
            let originalLayout = { ...layouts };
            // 拖拽到其他容器后的布局
            let selectedItemLayout = _.pick(originalLayout, keys);
            let switchedLayout = _.omit(originalLayout, keys);
            props.onFlyStart?.(originalLayout, selectedItemLayout);
            // solve the neighbor collision problem when dragging multiple comps
            const leftAdjacentItems = calcLeftAdjacentItems(selectedItemLayout);
            // position params
            const positionParams = genPositionParams(props);
            // coordinate
            let { offsetX, offsetY } = calcOffset(e, ref.current as HTMLDivElement, positionParams, draggingItemPosition.current, transformScale);
            draggingItemPosition.current = {
                x: Math.ceil(offsetX / positionParams.colWidth),
                y: Math.ceil(offsetY / positionParams.rowHeight)
            };

            // 恢复初始布局状态
            const recoverDragStartFn = () => {
                SetLayoutOPSWrapper([], " 恢复-恢复操作");
                setLayoutsWrapper(originalLayout, " 恢复-恢复布局");
            };

            // 保存拖拽初始信息
            draggingUtils.setData(FLY_START_INFO, {
                originalLayout: originalLayout,
                switchedLayout: switchedLayout,
                currentItemID: i,
                flyItemKeys: keys,
                flyItemLayouts: selectedItemLayout,
                flyExtraLayout: extraLayout,
                flyLeftAdjItems: leftAdjacentItems,
                flyPositionParams: positionParams,
                colWidth: calcGridColWidth(positionParams),
                flyStartPosition: { x: offsetX, y: offsetY },
                flyStartRecoverFn: recoverDragStartFn,
                containerRef: ref.current,
            });
        }

        /**
         * 拖拽事件顺序：2
         * 1. 检查是否在画布内
         * 2. 计算新的网格位置
         * 3. 如果位置发生变化，更新拖拽项的位置
         * 4. 通过 setStateResizeOPS 更新布局
         */
        const onDrag = (i: string, e: DragEvent<HTMLDivElement>, node: HTMLDivElement) => {
            const flyStartInfo = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO);
            // 未在画布中或未偏移格子不响应拖拽
            if ((e.clientX === 0 && e.clientY === 0 && e.screenX === 0 && e.screenY === 0)) return;
            const currentContainerRef = draggingUtils.getData<string>(CURRENT_CONTAINER_REF);
            if (currentContainerRef !== ref.current) {
                if (!deletePlaceholderAndHideDraggingCompRef.current) {
                    deletePlaceholderAndHideDraggingCompRef.current = true;
                    SetLayoutOPSWrapper([deleteItemOp(ADD_NEW_COMP_KEY),
                    ...(flyStartInfo?.flyItemKeys.map(k => changeItemOp(k, { ...layouts[k], hide: true })) ?? [])
                    ], "拖拽-拖入其他容器隐藏组件操作");
                }
                return;
            }
            const positionParams = flyStartInfo.flyPositionParams;
            const { offsetX, offsetY, x, y } = calcOffset(
                e,
                ref.current as HTMLDivElement,
                positionParams,
                draggingItemPosition.current,
                props.transformScale
            );
            if (x !== draggingItemPosition.current.x || y !== draggingItemPosition.current.y) {
                draggingItemPosition.current = { x, y };
                const items = getFinalFlyingItems(offsetX, offsetY);
                const ops = items.map((item) => changeItemOp(item.i, item))
                SetLayoutOPSWrapper(ops, "拖拽-当前容器拖拽操作");
            }
        };

        /**
         * 拖拽事件顺序：3
         * 1. 检查是否正在拖拽
         * 2. 清除原始布局引用
         * 3. 更新拖拽项状态(移除 isDragging 和 placeholder 标记)
         * 4. 重置画布内计数
         */
        const onDragEnd = (i: string, e: DragEvent<HTMLDivElement>, node: HTMLDivElement) => {
            if (!draggingUtils.isDragging()) return;
            const currentContainerRef = draggingUtils.getData<string>(CURRENT_CONTAINER_REF);
            const dropEventProcessed = draggingUtils.getData<boolean>(DROP_EVENT_PROCESSED);
            const flyStartInfo = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO);
            if (currentContainerRef !== ref.current && !dropEventProcessed) {
                const flySwitch = draggingUtils.getData<FlySwitchFnType>(FLY_SWITCH_FN)
                if (flySwitch) flySwitch();
                flyStartInfo.flyStartRecoverFn();
            } else if (currentContainerRef === ref.current && !dropEventProcessed) {
                // 在当前容器拖动
                originalLayoutRef.current = {}
                const ops = layoutOpUtils.push(flyStartInfo.flyItemKeys.map((item) => changeItemOp(item, { ...layouts[item], isDragging: undefined, placeholder: undefined, hide: undefined })),
                    flagItemOp(Date.now())
                )
                SetLayoutOPSWrapper(ops, "拖拽结束-当前容器");
                inCanvasCountRef.current = 0;
                useEditorStore.getState().setDragging(false);
            } else {
                // 组件拖到其他容器
                compsHeightMap.current = _.omit(compsHeightMap.current, flyStartInfo.flyItemKeys)
                SetLayoutOPSWrapper(
                    [deleteItemOp(ADD_NEW_COMP_KEY),
                    ...(flyStartInfo.flyItemKeys.map(k => deleteItemOp(k)) ?? []),
                    flagItemOp(Date.now())
                    ], "拖拽结束-其他容器");
            }
        };

        return (
            <GridItem
                isDragging={!!item.isDragging}
                showGridLines={props.showGridLines ?? false}
                compType={itemExtraInfo?.compType}
                key={item.i}
                delaying={frozenCompNameRef.current === item.i && isShowFrozenLayout() === false}
                containerWidth={props.gridWidth!}
                cols={props.cols!}
                margin={props.margin!}
                containerPadding={positionParams.containerPadding}
                maxRows={props.maxRows!}
                rowHeight={props.rowHeight!}
                onDragStart={onDragStart}
                onDrag={onDrag}
                onDragEnd={onDragEnd}
                colWidth={positionParams.colWidth}
                onResizeStart={onResizeStart}
                onResize={onResize}
                onResizeStop={onResizeStop}
                onHeightChange={onHeightChange}
                isDraggable={props.isDraggable! && isItemDraggable(item)}
                isResizable={props.isResizable! && isItemResizable(item)}
                isSelectable={props.isSelectable!}
                transformScale={props.transformScale!}
                w={item.w}
                h={itemExtraInfo?.hidden && !itemExtraInfo?.isSelected ? 0 : item.h}
                x={item.x}
                y={item.y}
                i={item.i}
                itemMargin={itemExtraInfo?.margin}
                minH={item.minH}
                minW={item.minW}
                maxH={item.maxH}
                maxW={item.maxW}
                placeholder={item.placeholder}
                layoutHide={item.hide}
                static={item.static}
                resizeHandles={getItemResizeHandles(item, itemExtraInfo)}
                name={itemExtraInfo?.name}
                autoHeight={itemExtraInfo?.autoHeight}
                isSelected={itemExtraInfo?.isSelected}
                hidden={itemExtraInfo?.hidden}
                selectedSize={props.selectedSize}
                clickItem={props.clickItem}
                showName={showNameProps}
            >
                {getGridItemChild(item, child, isPlaceholder, itemExtraInfo)}
            </GridItem>
        );
    }


    const getDefaultStyle = useMemo(() => {
        let minHeight = props.minHeight;
        const flyOverInfo = draggingUtils.getData<FlyOverInfo>(FLY_OVER_INFO);
        if (flyOverInfo?.layoutRef === ref) {
            const flyOverMinHeight = flyOverInfo.innerHeight + "px";
            minHeight = minHeight ? `max(${minHeight}, ${flyOverMinHeight})` : flyOverMinHeight;
        }

        const style: Record<string, any> = {
            minHeight: props.autoHeight ? minHeight ?? "100%" : "100%",
        };
        return style;
    }, [props.minHeight, props.autoHeight])

    const containerHeight = () => {
        const { margin, rowHeight } = props as Required<GridLayoutProps>;
        const { extraHeight, emptyRows } = props;
        const { containerPadding } = positionParams;
        let nbRow = layoutBottomRef.current;
        if (!_.isNil(emptyRows) && _.size(layouts) === 0) {
            nbRow = emptyRows;
        }
        const containerHeight = Math.max(
            nbRow * rowHeight + (nbRow - 1) * margin[1] + containerPadding[1] * 2, WapperHeight ?? 0
        );
        // log.debug("layout: containerHeigh=", containerHeight, " minHeight: ", this.props.minHeight);
        const height = extraHeight
            ? `calc(${containerHeight}px + ${extraHeight})`
            : containerHeight + "px";
        // log.log( "containerHeight. nbRow: ", nbRow, " containerPadding: ", containerPadding[1], " containerHeight: ", containerHeight, " height: ", height);
        return height;
    }

    const contentStyle = {
        height: containerHeight(),
        ...getDefaultStyle,
    };

    const getFinalDroppingItem = (
        onDragOverResult:
            | { size: { w?: number; h?: number }; positionParams?: PositionParams }
            | undefined,
        left: number,
        top: number
    ): LayoutItem => {
        const droppingItem = {
            i: ADD_NEW_COMP_KEY,
            ...onDragOverResult?.size,
        };
        let { w, h } = droppingItem as Required<LayoutItem>;
        const positionParams: PositionParams = genPositionParams(props);
        // convert grid units, in order to keep comps' size stable when they are through containers
        const sourcePositionParams = onDragOverResult?.positionParams;
        if (sourcePositionParams) {
            const { width, height } = calcGridItemSizePx(sourcePositionParams, w, h);
            // log.debug("layout: sourceTransform. w: ", w, " h: ", h, " sourcePositinParams: ", sourcePositionParams, " width: ", width, " height: ", height);
            const { w: ww, h: hh } = calcWH(positionParams, width, height, {
                w: false,
                h: false,
                ceil: false,
            });
            [w, h] = [ww, hh];
        }
        const { width, height } = calcGridItemSizePx(positionParams, w, h);
        // log.debug("layout: getFinalDroppingItem. w: ", w, " h: ", h, " sourcePositinParams: ", sourcePositionParams, " width: ", width, " height: ", height);
        left -= width * 0.5;
        top -= height * 0.5 - positionParams.containerPadding[0];
        const calculatedPosition = calcXY(positionParams, top, left, w, h, { x: false, y: true });
        let finalItem = {
            ...droppingItem,
            w: w,
            h: h,
            x: calculatedPosition.x,
            y: calculatedPosition.y,
            isDragging: true,
        } as LayoutItem;
        finalItem = narrow(finalItem, 0, positionParams.cols);
        return finalItem;
    }

    function getNewLayoutItem(
        e: React.KeyboardEvent,
        isResize: boolean,
        item: LayoutItem,
        extraItem?: ExtraItem
    ) {
        if (isResize) {
            if (isItemResizable(item)) {
                if (e.key === "ArrowLeft" || e.key === "ArrowRight") {
                    if (canResizeRight(item, extraItem)) {
                        return { ...item, w: item.w + (e.key === "ArrowLeft" ? -1 : 1) };
                    }
                } else if (e.key === "ArrowUp" || e.key === "ArrowDown") {
                    if (canResizeBottom(item, extraItem)) {
                        return { ...item, h: item.h + (e.key === "ArrowUp" ? -1 : 1) };
                    }
                }
            }
        } else if (isItemDraggable(item)) {
            const x = item.x + (e.key === "ArrowLeft" ? -1 : e.key === "ArrowRight" ? 1 : 0);
            const y = item.y + (e.key === "ArrowUp" ? -1 : e.key === "ArrowDown" ? 1 : 0);
            return { ...item, x, y };
        }
    }
    const moveOrResize = (
        e: React.KeyboardEvent,
        isResize: boolean,
        layout: Layout,
        extraLayout: ExtraLayout,
        positionParams: PositionParams
    ) => {
        const selectedKeys = Object.keys(layout).filter((k) => extraLayout[k]?.isSelected);
        if (selectedKeys.length === 0 || (isResize && selectedKeys.length !== 1)) {
            return;
        }
        e.preventDefault();
        const newSelectLayout: Layout = {};
        const newOps: LayoutOps = [];
        for (const key of selectedKeys) {
            const newItem = getNewLayoutItem(e, isResize, layout[key], extraLayout[key]);
            if (!newItem || !isValidLayoutItem(newItem, positionParams)) {
                return;
            }
            newSelectLayout[key] = newItem;
            newOps.push(changeItemOp(key, newItem));
        }
        const otherKeys = Object.keys(layout).filter((k) => !newSelectLayout[k]);
        for (const newItem of Object.values(newSelectLayout)) {
            if (otherKeys.some((k) => collides(newItem, layout[k]))) {
                return;
            }
        }
        return newOps;
    }
    const onKeyDown = (e: React.KeyboardEvent) => {
        if (props.disableDirectionKey || !isDirectionKey(e) || isFilterInputTarget(e)) {
            return;
        }
        const isResize = modKeyPressed(e);
        if (isResize ? !props.isResizable : !props.isDraggable) {
            return;
        }
        const newOps = moveOrResize(
            e,
            isResize,
            getUILayouts(layouts),
            props.extraLayout ?? {},
            genPositionParams(props)
        );
        if (!newOps || newOps.length === 0) {
            return;
        }
        newOps.push(flagItemOp(Date.now()));
        SetLayoutOPSWrapper(newOps, "键盘操作-移动或调整大小");
    }

    // 拖动切换函数
    const flySwitchFn: FlySwitchFnType = () => {
        const flyStartInfo = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO);
        if (flyStartInfo) {
            // 取消移入组件
            if (flyStartInfo.containerRef !== ref.current) {
                // 其他容器取消拖入
                SetLayoutOPSWrapper([], "切换-取消跨容器操作")
                setLayoutsWrapper(props.layout, "切换-取消跨容器拖动并恢复布局")
            } else {
                // 当前容器移出
                // setLayoutOPS([])
                // setLayouts(flyStartInfo.switchedLayout)
                SetLayoutOPSWrapper([deleteItemOp(ADD_NEW_COMP_KEY),
                ...(flyStartInfo?.flyItemKeys.map(k => changeItemOp(k, { ...layouts[k], hide: true })) ?? [])
                ], "切换-从当前容器移出");
            }
        } else {
            // 取消添加组件
            SetLayoutOPSWrapper([], "切换-取消添加组件操作");
            setLayoutsWrapper(props.layout, "切换-取消添加组件恢复布局");
        }
    }

    const mayScroll = (offsetY: number) => {
        const element = props.scrollContainerRef?.current;
        if (!element) return;
        const topRatio = (offsetY - element.scrollTop) / element.clientHeight;
        edgeScroll(element, topRatio);
    }
    /**
     * 拖拽事件顺序：6
     * 1. 计算新的网格位置
     * 2. 如果位置发生变化：
     *    - 如果是新组件拖入，创建新的拖拽项
     *    - 如果是已有组件拖拽，更新现有组件位置
     */
    const onDragOver = (e: DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        if (draggingUtils.getData<string>(CURRENT_CONTAINER_REF) !== ref.current) {
            draggingUtils.setData(CURRENT_CONTAINER_REF, ref.current);
            const flySwitch = draggingUtils.getData<FlySwitchFnType>(FLY_SWITCH_FN)
            if (flySwitch) flySwitch();
            draggingUtils.setData(FLY_SWITCH_FN, flySwitchFn);
        }
        const flyStartInfo = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO);
        // 相对画布的偏移量，剔除容器padding
        const { offsetX, offsetY, x, y } = calcOffset(
            e,
            ref.current as HTMLDivElement,
            positionParams,
            dragOverItemPosition.current,
            props.transformScale
        );
        mayScroll(offsetY);
        if (x !== dragOverItemPosition.current.x || y !== dragOverItemPosition.current.y) {
            dragOverItemPosition.current = { x, y };
            if (!flyStartInfo) {
                // 组件列表向画布添加组件
                originalLayoutRef.current = { ...props.layout };
                const onDragOverResult = props?.onDropDragOver?.(e);
                const item = getFinalDroppingItem(onDragOverResult, offsetX, offsetY);
                const ops = changeItemOp(item.i, { ...item, hide: undefined })
                SetLayoutOPSWrapper([ops], "拖拽悬停-添加组件操作");
            } else if (flyStartInfo.containerRef !== ref.current) {
                // 组件在其他容器拖拽时
                originalLayoutRef.current = { ...props.layout };
                const item = getFinalFlyingItems(offsetX, offsetY);
                const ops = item.map((item) => changeItemOp(item.i, item))
                SetLayoutOPSWrapper(ops, "拖拽悬停-拖入其他容器");
            }
        }
    }

    /**
     * 拖拽事件顺序：7
     * 1. 如果是画布内拖拽，逻辑在 onDragEnd  中处理
     * 2. 如果是新组件拖入：
     *    - 清除拖拽项
     *    - 调用 onDrop 回调
     *    - 清除拖拽数据
     */
    const onDrop: DragEventHandler<HTMLElement> = (e) => {
        e.preventDefault();
        e.stopPropagation();
        const { onDrop } = props as Required<GridLayoutProps>;
        const flyStartInfo = draggingUtils.getData<FlyStartInfo>(FLY_START_INFO);
        if (flyStartInfo) {
            if (flyStartInfo.containerRef !== ref.current) {
                draggingUtils.setData(DROP_EVENT_PROCESSED, true);
                // 处理其他容器组件拖入当前容器
                let flyItemLayouts = layouts;
                const pickLayouts = _.pick(layouts, flyStartInfo.flyItemKeys)
                // 应对快速拖拽情况
                if (_.isEmpty(pickLayouts)) {
                    inCanvasCountRef.current = 0;
                    SetLayoutOPSWrapper(flyStartInfo.flyItemKeys.map(item => deleteItemOp(item)), "放置-拖出失败回车操作");
                    flyStartInfo.flyStartRecoverFn();
                    draggingUtils.clearData();
                    return
                }
                const ops = [...flyStartInfo.flyItemKeys.map((item) => changeItemOp(item, { ...flyItemLayouts[item], isDragging: undefined, placeholder: undefined, hide: undefined })),
                flagItemOp(Date.now())
                ]
                SetLayoutOPSWrapper(ops, "放置-拖动到其他容器");
                const layout = getUILayouts(layouts, ops);
                props.onFlyDrop?.(layout, _.pick(layout, flyStartInfo.flyItemKeys));
            }
        } else {
            originalLayoutRef.current = {}
            // 处理组件列表向容器添加组件
            if (layouts.hasOwnProperty(ADD_NEW_COMP_KEY)) {
                const items = _.pick(layouts, ADD_NEW_COMP_KEY);
                let layout = _.omit(layouts, ADD_NEW_COMP_KEY);
                SetLayoutOPSWrapper([deleteItemOp(ADD_NEW_COMP_KEY)], "放置-添加新组件");
                onDrop?.(layout, items, e);
            }
            draggingUtils.clearData();
        }
        inCanvasCountRef.current = 0;

    };

    /**
     * 拖拽事件顺序：5
     * 1. 减少画布内计数
     * 2. 如果计数为0，删除拖拽项
     */
    const onDragLeave = (e: DragEvent<HTMLElement>) => {
        e.preventDefault();
        e.stopPropagation();
        inCanvasCountRef.current--;
        dragOverItemPosition.current = { x: -1, y: -1 };
        // 添加组件时鼠标拖到画布外，放弃拖入操作
        if (inCanvasCountRef.current === 0 && layoutOPS.length > 0 && _.get(layoutOPS[0], 'key') === ADD_NEW_COMP_KEY)
            SetLayoutOPSWrapper([], "离开-鼠标离开画布");
    }

    /**
     * 拖拽事件顺序：4
     * 1. 增加画布内计数
     */
    const onDragEnter = (e: DragEvent<HTMLElement>) => {
        e.preventDefault();
        e.stopPropagation();
        inCanvasCountRef.current++;
    }

    // 根据 autoHeight 和 showScroll 自动计算 style.height
    // 如果用户已经传入了 style.height，优先使用用户的值
    const computedStyle = useMemo(() => {
        const userStyle = props.style || {};
        // 如果用户已经设置了 height，使用用户的值
        if (userStyle.height !== undefined) {
            return userStyle;
        }
        // 自动计算 height：
        // - 如果 showScroll 为 true，不设置 height（让内容自然滚动）
        // - 如果 autoHeight 为 true，不设置 height（让内容自适应）
        // - 如果 showScroll 为 false 且 autoHeight 为 false，设置 height 为 '100%'（填满容器）
        const computedHeight = (props.showScroll || props.autoHeight) ? undefined : '100%';
        return {
            ...userStyle,
            height: computedHeight,
        };
    }, [props.style, props.showScroll, props.autoHeight]);

    return (
        (!props.autoHeight && props.showScroll ? <Wapper ref={wapperRef}>
            <ScrollBar style={{height: WapperHeight+'px'}}>
                <LayoutContainer
                    ref={props.innerRef}
                    style={computedStyle}
                    $bgColor={props.bgColor}
                    $radius={props.radius}
                    $autoHeight={props.autoHeight}
                    $overflow={props.overflow}
                    tabIndex={-1}
                    className={mergedClassName}
                    onDrop={props.isDroppable ? onDrop : _.noop}
                    onDragLeave={props.isDroppable ? onDragLeave : _.noop}
                    onDragEnter={props.isDroppable ? onDragEnter : _.noop}
                    onDragOver={props.isDroppable ? onDragOver : _.noop}
                    onKeyDown={onKeyDown}
                >
                    {/* <div style={{ height: "0px" }}>children:{_.size(props.children)},props.layout:{_.size(props.layout)},layout:{_.size(layouts)},count:{inCanvasCountRef.current},stateChangedHs:{JSON.stringify(compsHeightMap)},dragOverPos: {JSON.stringify(dragOverItemPosition.current)}</div> */}
                    <div style={contentStyle} ref={ref}>
                        {props.showGridLines && <GridLines positionParams={positionParams} position={gridLinesPosition} lineColor={contrastBgColor} />}
                        {_.isEmpty(layouts) && hintPlaceholder
                            ? hintPlaceholder
                            : mounted && _.orderBy(layouts, ['z'], ['asc']).map((item) => processGridItem(item))}
                    </div>
                </LayoutContainer>
            </ScrollBar>
        </Wapper>:
        <LayoutContainer
        ref={props.innerRef}
        style={computedStyle}
        $bgColor={props.bgColor}
        $radius={props.radius}
        $autoHeight={props.autoHeight}
        $overflow={props.overflow}
        tabIndex={-1}
        className={mergedClassName}
        onDrop={props.isDroppable ? onDrop : _.noop}
        onDragLeave={props.isDroppable ? onDragLeave : _.noop}
        onDragEnter={props.isDroppable ? onDragEnter : _.noop}
        onDragOver={props.isDroppable ? onDragOver : _.noop}
        onKeyDown={onKeyDown}
    >
        {/* <div style={{ height: "0px" }}>children:{_.size(props.children)},props.layout:{_.size(props.layout)},layout:{_.size(layouts)},count:{inCanvasCountRef.current},stateChangedHs:{JSON.stringify(compsHeightMap)},dragOverPos: {JSON.stringify(dragOverItemPosition.current)}</div> */}
        <div style={contentStyle} ref={ref}>
            {props.showGridLines && <GridLines positionParams={positionParams} position={gridLinesPosition} lineColor={contrastBgColor} />}
            {_.isEmpty(layouts) && hintPlaceholder
                ? hintPlaceholder
                : mounted && _.orderBy(layouts, ['z'], ['asc']).map((item) => processGridItem(item))}
        </div>
    </LayoutContainer>
        )
    )
}