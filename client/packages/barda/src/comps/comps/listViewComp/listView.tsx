import { Pagination } from "antd";
import { useEditorStore } from "comps/editorStore";
import { BackgroundColorContext } from "comps/utils/backgroundColorContext";
import _ from "lodash";
import { changeChildAction, ConstructorToView, deferAction } from "barda-core";
import { HintPlaceHolder, pageItemRender } from "barda-design";
import { RefObject, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { ResizePayload, useResizeDetector } from "react-resize-detector";
import styled from "styled-components";
import { checkIsMobile } from "util/commonUtils";
import { useDelayState } from "util/hooks";
import { SimpleContainerComp } from "../containerBase/simpleContainerComp";
import {
  ContainerBaseProps,
  gridItemCompToGridItems,
  InnerGrid,
} from "../containerComp/containerView";
import { ContextContainerComp } from "./contextContainerComp";
import { ListViewImplComp } from "./listViewComp";
import { getCurrentItemParams, getData } from "./listViewUtils";
import { JSONObject } from "@barda/util/jsonTypes";
import { scaleNumbersInString } from "@barda/util/stringUtils";

const ListViewWrapper = styled.div<{ $style: any; $paddingWidth: string }>`
  height: 100%;
  border: 1px solid ${(props) => props.$style.border};
  border-radius: ${(props) => props.$style.radius};
  padding: ${(props) => props.$paddingWidth};
  background-color: ${(props) => props.$style.background};
`;

const FooterWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
`;

const BodyWrapper = styled.div<{ $autoHeight: boolean }>`
  overflow: auto;
  overflow: overlay;
  height: ${(props) => (props.$autoHeight ? "100%" : "calc(100% - 32px)")};
`;

const FlexWrapper = styled.div`
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const ContainerInListView = (props: ContainerBaseProps) => {
  return (
    <InnerGrid
      {...props}
      emptyRows={15}
      containerPadding={[4, 4]}
      hintPlaceholder={HintPlaceHolder}
    />
  );
};

type ListItemProps = {
  itemIdx: number;
  offset: number;
  containerProps: ConstructorToView<typeof SimpleContainerComp>;
  autoHeight: boolean;
  scrollContainerRef?: RefObject<HTMLDivElement>;
  minHeight?: string;
  unMountFn?: () => void;
  selectedIndex: number;
  selectIndexDispatch: any;
};

function ListItem(props: ListItemProps) {
  const { itemIdx, offset, containerProps, autoHeight, scrollContainerRef, minHeight } = props;
  const isFirstItem = itemIdx === offset;
  const gridItems = useMemo(() => gridItemCompToGridItems(containerProps.items), [containerProps.items]);
  const [hover, setHover] = useState(false);

  const style = useMemo(() => ({
    height: "100%",
    margin: "2px",
    backgroundColor: hover ? "#f2f2f2ff" : "transparent",
    flex: "auto",
    border: itemIdx === props.selectedIndex ? "2px solid #99B9ED" : "2px solid #d9d9d9",
    borderRadius: "5px",
    cursor: "pointer",
  }), [hover, itemIdx, props.selectedIndex]);
  const onClick = useCallback(() => {
    props.selectIndexDispatch(changeChildAction("selectedIndex", itemIdx, false));
  }, [itemIdx, props])
  const onMouseEnter = useCallback(() => setHover(true), [])
  const onMouseLeave = useCallback(() => setHover(false), [])

  const dispatch = useMemo(() => isFirstItem ? containerProps.dispatch : _.noop, [containerProps.dispatch, isFirstItem]);
  return (
    <div style={style}
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      <ContainerInListView
        layout={containerProps.layout}
        items={gridItems}
        positionParams={containerProps.positionParams}
        // 所有的布局更改都只应反映在 commonContainer 上
        dispatch={dispatch}
        style={{ height: "100%", flex: "auto", backgroundColor: "transparent" }}
        autoHeight={autoHeight}
        isDroppable={isFirstItem}
        isDraggable={isFirstItem}
        isResizable={isFirstItem}
        isSelectable={isFirstItem}
        scrollContainerRef={scrollContainerRef}
        overflow={"hidden"}
        minHeight={minHeight}
        enableGridLines={true}
      />
    </div>
  );
}

type Props = {
  comp: InstanceType<typeof ListViewImplComp>;
};

export function ListView(props: Props) {
  // console.info("<---- listView renders.");
  const isDragging = useEditorStore((s) => s.isDragging);
  const [listHeight, setListHeight] = useDelayState(0, isDragging);
  const onResize = useCallback((payload: ResizePayload) => {
    if (payload.height) setListHeight(payload.height);
  }, [setListHeight])
  const { ref } = useResizeDetector({
    onResize,
    refreshMode: "throttle",
    refreshRate: 50
  });
  const { comp } = props;
  const children = comp.children;
  const renderFlag = useRef(0);
  const [renders, setRenders] = useState<any>();
  const [data, setData] = useState<JSONObject[]>([])
  const [totalCount, setTotalCount] = useState(0)

  // 获取事件处理器
  const onEventHandler = useMemo(() => children.onEvent.getView(), [children.onEvent]);

  // 监听 selectIndex 变化，触发事件
  const prevSelectIndex = useRef(comp.children.selectIndex.value);
  useEffect(() => {
    const currentIndex = comp.children.selectIndex.value;
    if (prevSelectIndex.current !== currentIndex) {
      prevSelectIndex.current = currentIndex;
      onEventHandler("change");
    }
  }, [comp.children.selectIndex.value, onEventHandler]);
  useEffect(() => {
    if ((children.noOfRows as any).loading === false) {
      const { data, itemCount: totalCount } = getData(children.noOfRows.getView())
      setData(data)
      setTotalCount(totalCount)
    }
  }, [children.noOfRows])
  const dynamicHeight = useMemo(() => children.dynamicHeight.getView(), [children.dynamicHeight]);
  const heightUnitOfRow = useMemo(
    () => children.heightUnitOfRow.getView(),
    [children.heightUnitOfRow]
  );
  const containerFn = useMemo(() => children.container.getView(), [children.container]);
  const itemIndexName = useMemo(() => children.itemIndexName.getView(), [children.itemIndexName]);
  const itemDataName = useMemo(() => children.itemDataName.getView(), [children.itemDataName]);
  const autoHeight = useMemo(() => children.autoHeight.getView(), [children.autoHeight]);
  const noOfColumns = useMemo(
    () => Math.max(1, children.noOfColumns.getView()),
    [children.noOfColumns]
  );
  const pageInfo = useMemo(() => {
    const pagination = children.pagination.getView();
    const total = pagination.total || totalCount;
    let current = pagination.current;
    let offset = (current - 1) * pagination.pageSize;
    const currentPageSize = Math.max(0, Math.min(pagination.pageSize, total - offset));
    return {
      pagination: { ...pagination, current: current, total: total },
      offset,
      currentPageSize,
      total,
    };
  }, [children.pagination, totalCount]);

  useEffect(() => {
    renderFlag.current += 1;
    if (pageInfo.offset.toString() === (children.container as any).selection) {
      if (renderFlag.current > 0) {
        setRenders(getRenders);
      }
    } else {
      if (pageInfo.total !== data.length)
        renderFlag.current = -1;
    }
  }, [children.container, pageInfo, data, isDragging, props.comp.children.selectIndex, noOfColumns, itemIndexName, itemDataName])
  const style = children.style.getView();

  const commonLayout = comp.realSimpleContainer()!.children.layout.getView();
  const isOneItem =
    pageInfo.currentPageSize > 0 && (_.isEmpty(commonLayout) || isDragging);
  const noOfRows = isOneItem
    ? 1
    : Math.floor((pageInfo.currentPageSize + noOfColumns - 1) / noOfColumns);
  const rowHeight = isOneItem ? "100%" : dynamicHeight ? "auto" : heightUnitOfRow * 44 + "px";

  // minHeight is used to ensure that the container height will not shrink when dragging, and the current padding needs to be subtracted during calculation
  const minHeight = isDragging && autoHeight ? listHeight + "px" : "100%";
  // log.log("List. listHeight: ", listHeight, " minHeight: ", minHeight);
  const getRenders = () => _.range(0, noOfRows).map((rowIdx) => {
    const render = (
      <div
        key={rowIdx}
        style={{
          height: rowHeight,
        }}
      >
        <FlexWrapper>
          {_.range(0, noOfColumns).map((colIdx) => {
            const itemIdx = rowIdx * noOfColumns + colIdx + pageInfo.offset;
            if (
              itemIdx >= pageInfo.total ||
              itemIdx >= pageInfo.offset + pageInfo.pagination.pageSize ||
              (isOneItem && itemIdx > pageInfo.offset)
            ) {
              return <div key={itemIdx} style={{ flex: "auto" }}></div>;
            }
            const containerProps = containerFn(
              { [itemIndexName]: itemIdx, [itemDataName]: getCurrentItemParams(data, pageInfo.total === totalCount ? itemIdx : itemIdx - pageInfo.offset) },
              String(itemIdx)
            ).getView();
            const unMountFn = () => {
              comp.children.container.dispatch(
                deferAction(ContextContainerComp.batchDeleteAction([String(itemIdx)]))
              );
            };
            return (
              <ListItem
                key={itemIdx}
                itemIdx={itemIdx}
                offset={pageInfo.offset}
                containerProps={containerProps}
                autoHeight={isDragging || dynamicHeight}
                // scrollContainerRef={ref}
                minHeight={minHeight}
                unMountFn={unMountFn}
                selectedIndex={comp.children.selectIndex.value}
                selectIndexDispatch={props.comp.children.selectIndex.dispatch}
              />
            );
          })}
        </FlexWrapper>
      </div>
    );
    return render;
  });

  const maxWidth = useEditorStore((s) => s.rootComp?.children.settings.getView().maxWidth);
  const isMobile = checkIsMobile(maxWidth);
  const paddingWidth = isMobile ? scaleNumbersInString(style.padding_UNIT, 0.25, { type: 'round' },) : style.padding_UNIT;
  const divStyle = useMemo(() => ({ height: autoHeight ? "auto" : "100%" }), [autoHeight]);
  // log.debug("renders: ", renders);
  return (
    <BackgroundColorContext.Provider value={style.background}>
      <ListViewWrapper $style={style} $paddingWidth={paddingWidth}>
        <BodyWrapper ref={ref} $autoHeight={autoHeight}>
          <div style={divStyle}>{renders}</div>
        </BodyWrapper>
        <FooterWrapper>
          <Pagination size="small" itemRender={pageItemRender} {...pageInfo.pagination} />
        </FooterWrapper>
      </ListViewWrapper>
    </BackgroundColorContext.Provider>
  );
}
