import { StandardBoxMargin } from "@barda/comps/controls/styleControlConstants";
import { CloseEyeIcon, DragWhiteIcon, EllipsisTextCss, fadeColor, WidthDragIcon } from "barda-design";
import { UICompType } from "comps/uiCompRegistry";
import { Layers } from "constants/Layers";
import { ModulePrimaryColor, PrimaryColor } from "constants/style";
import React, {
  MouseEvent,
  MouseEventHandler,
  useCallback,
  useEffect,
  useRef,
  useState
} from "react";
import { useResizeDetector } from "react-resize-detector";
import styled, { css } from "styled-components";
import { getElementScrollPosition } from "./calculateUtils";
import { draggingUtils } from "./draggingUtils";
import { ResizeHandleAxis } from "./gridLayoutPropTypes";

export type DragHandleName = "w" | "e" | "nw" | "ne" | "sw" | "se";
type NamePos = "top" | "bottom" | "bottomInside";

const NameDiv = styled.div<{
  $isSelected: boolean;
  $position: NamePos;
  $compType: UICompType;
  $isDraggable: boolean;
  $margin: StandardBoxMargin;
}>`
  background: ${(props) => {
    if (props.$isSelected) {
      return props.$compType === "module" ? ModulePrimaryColor : PrimaryColor;
    }
    return "#B8B9BF";
  }};
  border-radius: ${(props) => (props.$position === "top" ? "2px 2px 0 0" : "0 0 2px 2px")};
  font-weight: 500;
  color: #ffffff;
  position: absolute;
  font-size: 12px;
  line-height: 16px;
  top: ${(props) => (props.$position === "top" ? "-16px" : "unset")};
  bottom: ${(props) =>
    props.$position === "top" ? "unset" : props.$position === "bottom" ? "-16px" : "0px"};
  height: 16px;
  right: 0;
  padding-right: 5px;
  padding-left: ${(props) => (props.$isDraggable ? 0 : "5px")};
  display: flex;
  cursor: ${(props) => (props.$isDraggable ? "grab" : "pointer")};
  z-index: 10;
`;
const NameLabel = styled.span`
  max-width: 208px;
  ${EllipsisTextCss};
`;

export const GRID_ITEM_BORDER_WIDTH = 1.5;

function getLineStyle(
  hover: boolean,
  showDashLine: boolean,
  isSelected: boolean,
  compType: UICompType,
  isHidden: boolean,
  margin: StandardBoxMargin,
  delaying: boolean,
) {
  const isModule = compType === "module";
  const primaryColor = isModule ? ModulePrimaryColor : PrimaryColor;
  let borderColor = "transparent";
  let borderStyle = "solid";
  if (isSelected || hover) {
    borderColor = primaryColor;
  } else if (showDashLine) {
    borderColor = fadeColor(primaryColor, 0.5);
    borderStyle = "dashed";
  }
  let marginArray = margin;
  if (compType === "module") {
    marginArray = [0, 0, 0, 0];
  }
  const noMargin = !!marginArray?.every(v => v === 0);
  return `
      border: ${GRID_ITEM_BORDER_WIDTH}px ${borderStyle} ${delaying ? '#FF6666' : borderColor};
      padding: ${isHidden && !isSelected ? 0 : marginArray?.map(v => `${v}px`).join(" ")};
      box-sizing: ${noMargin ? "content-box" : "border-box"};
  `;
}

const InnerWrapper = styled.div<{ $autoHeight: boolean }>`
  height: ${(props) => props.$autoHeight ? "auto" : "100%"};
`;

// padding: ${props => props.hover || props.showDashline ? 3 : 4}px;
const SelectableDiv = styled.div<{
  $hover: boolean;
  $showDashLine: boolean;
  $isSelected: boolean;
  $compType: UICompType;
  $isHidden: boolean;
  $needResizeDetector: boolean;
  $overflow?: boolean;
  $margin: StandardBoxMargin;
  $height?: number;
  $isDragging: boolean;
  $delaying: boolean;
}>`
  width: 100%;
  height: 100%;
  overflow: ${(props) => (props.$overflow ? "visible" : "hidden")};

  ${(props) =>
    `${getLineStyle(
      props.$hover,
      props.$showDashLine,
      props.$isSelected,
      props.$compType,
      props.$isHidden,
      props.$margin,
      props.$delaying,
    )}`}
  & .module-wrapper {
    margin: ${-GRID_ITEM_BORDER_WIDTH}px;
  }

  ${(props) =>
    props.$compType === "image" &&
    props.$needResizeDetector &&
    !props.$isHidden &&
    `
    display: inline-flex;
    align-items: center;
    > div:nth-last-of-type(1)
    {
      flex-grow: 1;
    }
  `}
`;

interface DragHandleProps {
  $compType: UICompType;
  $resizeHandles: ResizeHandleAxis[];
  $noMargin?: boolean;
}

const dragDisplay = (handle: ResizeHandleAxis, props: DragHandleProps) => {
  if (props.$resizeHandles.includes(handle)) {
    return "block";
  }
  return "none";
};

// draggable handles (height unchangable)
const dragIconCss = (props: DragHandleProps, handle: ResizeHandleAxis) => css`
  position: absolute;
  top: 50%;
  z-index: 1;
  pointer-events: none;
  color: ${props.$compType === "module" ? ModulePrimaryColor : PrimaryColor};
  display: ${dragDisplay(handle, props)};
`;

const DragLeftIcon = styled(WidthDragIcon) <DragHandleProps>`
  ${(props) => dragIconCss(props, "w")};
  left: -3.5px;
  transform: translate(0px, -50%);
`;

const DragRightIcon = styled(WidthDragIcon) <DragHandleProps >`
  ${(props) => dragIconCss(props, "e")};
  right: ${(props) => props.$noMargin ? "-6.5px" : "-3.5px"};
  transform: translate(0px, -50%);
`;

// (height changable)
const dragCss = (props: DragHandleProps, handle: ResizeHandleAxis) => css`
  position: absolute;
  height: 8px;
  width: 8px;
  border: 1px solid ${props.$compType === "module" ? ModulePrimaryColor : PrimaryColor};
  border-radius: 2px;
  background-color: #f5f5f6;
  z-index: 11;
  pointer-events: none;
  display: ${dragDisplay(handle, props)};
`;

const dragAutoHeightCss = (props: DragHandleProps, handle: ResizeHandleAxis) => css`
  position: absolute;
  min-height: 7px;
  width: 7px;
  border: 1px solid ${props.$compType === "module" ? ModulePrimaryColor : PrimaryColor};
  border-radius: 4px;
  background-color: #f5f5f6;
  z-index: 1;
  pointer-events: none;
  display: ${dragDisplay(handle, props)};
`;

const DragW = styled.div<DragHandleProps>`
  ${(props) => dragAutoHeightCss(props, "w")};
  left: -2.5px;
  top: 50%;
  transform: translate(0px, -50%);
`;
const DragE = styled.div<DragHandleProps>`
  ${(props) => dragAutoHeightCss(props, "e")};
  right: -2.5px;
  top: 50%;
  transform: translate(0px, -50%);
`;
const DragNW = styled.div<DragHandleProps>`
  ${(props) => dragCss(props, "nw")};
  left: -2.5px;
  top: -2.5px;
`;
const DragNE = styled.div<DragHandleProps>`
  ${(props) => dragCss(props, "ne")};
  right: ${(props) => props.$noMargin ? "-5px" : "-2.5px"};
  top: ${(props) => props.$noMargin ? "-5px" : "-2.5px"};
`;
const DragSW = styled.div<DragHandleProps >`
  ${(props) => dragCss(props, "sw")};
  left: ${(props) => props.$noMargin ? "-5px" : "-2.5px"};
  bottom: ${(props) => props.$noMargin ? "-5px" : "-2.5px"};
`;
const DragSE = styled.div<DragHandleProps>`
  ${(props) => dragCss(props, "se")};
  right: ${(props) => props.$noMargin ? "-5px" : "-2.5px"};
  bottom: ${(props) => props.$noMargin ? "-5px" : "-2.5px"};
`;

const HiddenIcon = styled(CloseEyeIcon)`
  g g {
    fill: #f5f5f6;
  }
`;


export const CompSelectionWrapper = (props: {
  id?: string;
  compType: UICompType;
  className?: string;
  style?: Record<string, any>;
  isSelected: boolean;
  autoHeight: boolean;
  placeholder?: boolean;
  onClick: MouseEventHandler<HTMLDivElement>;
  children: JSX.Element | React.ReactNode;
  hidden: boolean;
  nameConfig: {
    show: boolean;
    name: string | undefined;
    pos: NamePos;
  };
  onInnerResize: (width?: number, height?: number) => void;
  onWrapperResize: (width?: number, height?: number) => void;
  isSelectable: boolean;
  isDraggable: boolean;
  isResizable: boolean;
  resizeHandles: ResizeHandleAxis[];
  resizeIconSize: "small" | "normal";
  delaying?: boolean;
  isDragging: boolean;
  margin?: StandardBoxMargin;
  showGridLines: boolean;
}) => {
  const nameDivRef = useRef<HTMLDivElement>(null);
  let [hover, setHover] = useState(false);
  const noMargin = !!props.margin?.every(v => v === 0);
  const onMouseOver = useCallback(
    (e: MouseEvent<HTMLDivElement>) => {
      e.stopPropagation();
      // log.debug("onMouseOver. name: ", props.name, " hover: ", hover, " relateTarget: ", e.relatedTarget, " target: ", e.target);
      if (draggingUtils.isDragging()) return; // no hover when dragging
      // don't handle mouse events when moving from nameDiv
      let relatedTarget = e.relatedTarget;
      while (relatedTarget) {
        if (relatedTarget === nameDivRef.current) return;
        relatedTarget = (relatedTarget as any).parentNode;
      }
      setHover(true);
    },
    [setHover]
  );
  const onMouseOut = useCallback(
    (e: MouseEvent<HTMLDivElement>) => {
      e.stopPropagation();
      // log.debug("onMouseOut. name: ", props.name, " hover: ", hover, " relateTarget: ", e.relatedTarget, " target: ", e.target);
      // don't handle events moving to nameDiv
      let relatedTarget = e.relatedTarget;
      while (relatedTarget) {
        if (relatedTarget === nameDivRef.current) return;
        relatedTarget = (relatedTarget as any).parentNode;
      }
      setHover(false);
    },
    [setHover]
  );

  const selectableDivProps = props.isSelectable
    ? {
      onMouseOver,
      onMouseOut,
      onClick: props.onClick,
      $hover: hover,
      $showDashLine: props.showGridLines || props.hidden,
      $isSelected: props.isSelected,
      $isHidden: props.hidden,
    }
    : {
      $hover: false,
      $showDashLine: false,
      $isSelected: false,
      $isHidden: false,
    };

  const zIndex = props.isSelected
    ? Layers.compSelected
    : hover
      ? Layers.compHover
      : props.hidden
        ? Layers.compHidden
        : undefined;

  const { height: wrapperHeight, ref: wrapperRef } = useResizeDetector({
    handleHeight: props.autoHeight,
    handleWidth: false,
    refreshMode: "throttle",
    refreshRate: 50
  });
  const { height: innerHeight, ref: InnerWrapperRef } = useResizeDetector({
    handleHeight: props.autoHeight,
    handleWidth: false,
    refreshMode: "throttle",
    refreshRate: 50
  });
  useEffect(() => {
    if (wrapperHeight !== undefined) props.onWrapperResize(0, wrapperHeight)
  }, [wrapperHeight])

  useEffect(() => {
    if (innerHeight !== undefined) props.onInnerResize(0, innerHeight)
  }, [innerHeight])

  useEffect(() => {
    if (props.isSelected) {
      const scrollPostion = getElementScrollPosition(wrapperRef.current?.getBoundingClientRect())
      if (scrollPostion === "none") return;
      if (scrollPostion === "end") {
        wrapperRef.current?.scrollIntoView({ behavior: "smooth", block: scrollPostion })
      } else {
        nameDivRef.current?.scrollIntoView({ behavior: "smooth", block: scrollPostion })
      }
    }
  }, [props.isSelected])

  // log.debug("CompSelectionWrapper. name: ", props.name, " zIndex: ", zIndex);
  const { nameConfig, resizeIconSize } = props;
  return (
    <div id={props.id} style={{ ...props.style, zIndex }} className={props.className}>
      <SelectableDiv
        {...selectableDivProps}
        $compType={props.compType}
        ref={wrapperRef}
        $needResizeDetector={props.autoHeight}
        $margin={props.margin!}
        $height={innerHeight}
        $isDragging={props.isDragging}
        $delaying={props.delaying ?? false}
        // 如为按钮则overflow设置可溢出
        $overflow={!props.hidden && props.compType === 'button' && !props.isSelected}
      >
        {props.isSelectable && nameConfig.show && (hover || props.isSelected || props.hidden) && (
          <NameDiv
            $compType={props.compType}
            $isSelected={hover || props.isSelected}
            $position={nameConfig.pos}
            $isDraggable={props.isDraggable}
            ref={nameDivRef}
            $margin={props.margin!}
          >
            {props.isDraggable && <DragWhiteIcon />}
            <NameLabel>{nameConfig.name}</NameLabel>
            {props.hidden && <HiddenIcon />}
          </NameDiv>
        )}
        {props.isResizable &&
          props.isSelected &&
          props.autoHeight &&
          (resizeIconSize === "normal" ? (
            <>
              <DragLeftIcon $compType={props.compType} $resizeHandles={props.resizeHandles} />
              <DragRightIcon $compType={props.compType} $resizeHandles={props.resizeHandles} $noMargin={noMargin} />
            </>
          ) : (
            <>
              <DragE $compType={props.compType} $resizeHandles={props.resizeHandles} />
              <DragW $compType={props.compType} $resizeHandles={props.resizeHandles} />
            </>
          ))}
        {props.isResizable && props.isSelected && !props.autoHeight && (
          <>
            <DragNW $compType={props.compType} $resizeHandles={props.resizeHandles} />
            <DragNE $compType={props.compType} $resizeHandles={props.resizeHandles} $noMargin={noMargin} />
            <DragSW $compType={props.compType} $resizeHandles={props.resizeHandles} $noMargin={noMargin} />
            <DragSE $compType={props.compType} $resizeHandles={props.resizeHandles} $noMargin={noMargin} />
          </>
        )}
        <InnerWrapper ref={InnerWrapperRef} $autoHeight={props.autoHeight}>{props.children}</InnerWrapper>
      </SelectableDiv>
    </div>
  );
};
