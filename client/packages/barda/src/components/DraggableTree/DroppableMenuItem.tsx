import { useDraggable, useDroppable } from "@dnd-kit/core";
import { Fragment, useContext, memo, useMemo, useCallback } from "react";
import styled from "styled-components";
import DraggableItem from "./DraggableItem";
import { DraggableTreeContext } from "./DraggableTreeContext";
import DroppablePlaceholder from "./DroppablePlaceHolder";
import { DraggableTreeNode, DraggableTreeNodeItemRenderProps, IDragData, IDropData } from "./types";
import { checkDroppableFlag } from "./util";

const DraggableMenuItemWrapper = styled.div`
  position: relative;
`;

interface IDraggableMenuItemProps {
  path: number[];
  item: DraggableTreeNode;
  forceFold?: boolean;
  dropInAsSub?: boolean;
  activeNode?: DraggableTreeNode;
  disabled?: boolean;
  // current item is used as overlay or not
  isOverlay?: boolean;
  disableDropIn?: boolean;
  parentDragging?: boolean;
  onDelete?: (path: number[]) => void;
  onAddSubMenu?: (path: number[], value?: any) => void;
  renderContent?: (params: DraggableTreeNodeItemRenderProps) => React.ReactNode;
}

const DraggableMenuItem = memo((props: IDraggableMenuItemProps) => {
  const {
    item,
    path,
    activeNode,
    disabled,
    parentDragging,
    disableDropIn,
    forceFold = false,
    dropInAsSub = true,
    isOverlay = false,
    onDelete,
    renderContent,
  } = props;

  const id = item.id ?? path.join("_");
  const items = item.items;
  const context = useContext(DraggableTreeContext);
  const isFold = (forceFold || context.foldedStatus[id]) && !context.unfoldAll;

  const dragData: IDragData = {
    path,
    node: item,
  };
  const {
    listeners: dragListeners,
    setNodeRef: setDragNodeRef,
    isDragging,
  } = useDraggable({
    id,
    data: dragData,
    disabled: context.disable ?? false,
  });

  // 使用 useMemo 缓存检查结果
  const canDropIn = useMemo(() => checkDroppableFlag(item.canDropIn, activeNode?.data), [item.canDropIn, activeNode?.data]);
  const canDropBefore = useMemo(() => checkDroppableFlag(item.canDropBefore, activeNode?.data), [item.canDropBefore, activeNode?.data]);
  const canDropAfter = useMemo(() => checkDroppableFlag(item.canDropAfter, activeNode?.data), [item.canDropAfter, activeNode?.data]);

  // 使用 useMemo 缓存 dropData
  const dropData: IDropData = useMemo(() => ({
    targetListSize: items.length,
    targetPath: dropInAsSub ? [...path, 0] : [...path.slice(0, -1), path[path.length - 1] + 1],
    dropInAsSub,
  }), [items.length, path, dropInAsSub]);

  const { setNodeRef: setDropNodeRef, isOver } = useDroppable({
    id,
    disabled: isDragging || disabled || disableDropIn || (!dropInAsSub && canDropAfter === false),
    data: dropData,
  });

  // 使用 useCallback 优化事件处理函数
  const handleRef = useCallback((node: any) => {
    setDragNodeRef(node);
    setDropNodeRef(node);
  }, [setDragNodeRef, setDropNodeRef]);

  const handleDelete = useCallback(() => {
    onDelete?.(path);
  }, [onDelete, path]);

  const handleToggleFold = useCallback(() => {
    context.toggleFold(id);
  }, [context, id]);

  // 使用 useMemo 缓存渲染参数
  const renderParams = useMemo(() => ({
    node: item,
    isOver,
    path,
    isOverlay,
    hasChildren: items.length > 0,
    dragging: !!(isDragging || parentDragging),
    isFolded: isFold,
    onDelete: handleDelete,
    onToggleFold: handleToggleFold,
  }), [item, isOver, path, isOverlay, items.length, isDragging, parentDragging, isFold, handleDelete, handleToggleFold]);

  // 使用 useMemo 缓存子项渲染
  const subItems = useMemo(() => (
    items.length > 0 && !isFold && (
      <div className="sub-menu-list">
        {items.map((subItem, i) => (
          !subItem.hidden && (
            <Fragment key={i}>
              <DraggableMenuItem
                path={[...path, i]}
                activeNode={activeNode}
                dropInAsSub={false}
                item={subItem}
                renderContent={renderContent}
                disabled={disabled || isDragging || disableDropIn}
                onDelete={onDelete}
                parentDragging={isDragging}
              />
            </Fragment>
          )
        ))}
      </div>
    )
  ), [items, isFold, path, activeNode, renderContent, disabled, isDragging, disableDropIn, onDelete]);

  return (
    <>
      <DraggableMenuItemWrapper>
        {activeNode && canDropBefore && (
          <DroppablePlaceholder
            path={path}
            targetListSize={items.length}
            disabled={isDragging || disabled}
          />
        )}
        <DraggableItem
          path={path}
          id={id}
          dropInAsSub={dropInAsSub && canDropIn !== false}
          isOver={isOver}
          ref={handleRef}
          {...dragListeners}
        >
          {renderContent?.(renderParams) || null}
        </DraggableItem>
      </DraggableMenuItemWrapper>
      {subItems}
    </>
  );
});

// 添加组件显示名称，方便调试
DraggableMenuItem.displayName = "DraggableMenuItem";

export default DraggableMenuItem;
