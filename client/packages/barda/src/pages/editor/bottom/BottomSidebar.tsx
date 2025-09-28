import {
  BluePlusIcon,
  EditPopover,
  EditText,
  FoldedIcon,
  PointIcon,
  PopupCard,
  ScrollBar,
  Search,
  SearchIcon,
  TacoButton,
  UnfoldIcon,
} from "barda-design";
import { DraggableTree } from "components/DraggableTree/DraggableTree";
import {
  DraggableTreeNode,
  DraggableTreeNodeItemRenderProps,
} from "components/DraggableTree/types";
import RefTreeComp from "comps/comps/refTreeComp";
import { ActiveTextColor, BorderActiveColor, NormalMenuIconColor } from "constants/style";
import { trans } from "i18n";
import { match } from "pinyin-pro";
import { CSSProperties, memo, useCallback, useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { showAppSnapshotSelector } from "redux/selectors/appSnapshotSelector";
import styled from "styled-components";
import { BottomResComp, BottomResTypeEnum } from "types/bottomRes";

const Contain = styled.div`
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  width: 100%;
  background-color: #ffffff;
`;
const Title = styled.div`
  flex-shrink: 0;
  height: 40px;
  width: 100%;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  
`;
const TitleSpan = styled.span`
  font-weight: 500;
  font-size: 13px;
  color: #222222;
  line-height: 20px;
  display: inline-block;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  user-select: none;
`;
const TitleRight = styled.div`
  display: flex;
  align-items: center;
`;
const SearchWrapper = styled.div`
  height: 28px;
  padding: 0 16px;
  margin-bottom: 4px;
  .ant-input-affix-wrapper {
    height: 28px;
  }
`;
const SearchIconBtn = styled(SearchIcon)`
  height: 16px;
  width: 16px;
  margin-right: 16px;
  color: #9195a3;
  cursor: pointer;
  &:hover {
    color: ${ActiveTextColor};
  }
`;
const AddIcon = styled(BluePlusIcon)`
  height: 12px;
  width: 12px;
  margin-right: -4px!important;
`;
const AddBtn = styled(TacoButton)`
  &&& {
    height: 24px;
    width: 64px;
    padding: 0px 0px;
    background-color: #fafbff;
    color: #4965f2;
    border-color: #c9d1fc;
    display: flex;
    align-items: center;
    box-shadow: none;
  
    &:hover {
      color: #315efb;
      background-color: #f5faff;
      border-color: #c2d6ff;
    }
  
    &:focus {
      color: #315efb;
      background-color: #f5faff;
      border-color: #c2d6ff;
    }
  
    &:hover ${AddIcon} g {
      stroke: #315efb;
    }
  
    &:disabled,
    &:disabled:hover {
      background: #f9fbff;
      border: 1px solid #dee9ff;
      border-radius: 4px;
  
      ${AddIcon} g {
        stroke: #4965f230;
      }
    }
  }
`;

type RefTreeCompType = InstanceType<typeof RefTreeComp>;

const matchesPinyinSearch = (text: string, searchTerm: string): boolean => {
  if (!text || !searchTerm) return false;

  const lowerText = text.toLowerCase();
  const lowerSearchTerm = searchTerm.toLowerCase();

  if (lowerText.includes(lowerSearchTerm)) {
    return true;
  }

  const hasChinese = /[\u4e00-\u9fff]/.test(text);
  if (!hasChinese) {
    return false;
  }

  try {
    const matchResult = match(text, lowerSearchTerm);
    if (matchResult && matchResult.length > 0) {
      return true;
    }
  } catch (error) {
    console.warn('Pinyin conversion failed:', error);
  }

  return false;
};

interface BottomSidebarProps {
  selectedBottomResName: string;
  selectedBottomResType?: BottomResTypeEnum;
  style?: CSSProperties;
  dataSourceId?: string;
  refTreeComp: RefTreeCompType;
  items: BottomResComp[];
  onCopy: (type: BottomResTypeEnum, name: string) => void;
  onDelete: (type: BottomResTypeEnum, name: string) => boolean;
  onSelect: (type: BottomResTypeEnum, name: string) => void;
  onOpenCreatePanel: () => void;
  rename: (oldName: string, newName: string) => boolean;
  setSelectedBottomRes: (name: string, type: BottomResTypeEnum) => void;
  checkRename: (name: string, value: string) => string;
}

export function BottomSidebar(props: BottomSidebarProps) {
  const { items, refTreeComp, selectedBottomResName, selectedBottomResType,
    onOpenCreatePanel, onSelect, onCopy, onDelete, rename, setSelectedBottomRes, checkRename } = props;
  const readOnly = useSelector(showAppSnapshotSelector);
  const [isSearchShow, showSearch] = useState(false);
  const [search, setSearch] = useState("");

  const getById = useCallback((id: string) => items.find((i) => i.id() === id), [items]);

  const convertRefTree = useCallback((refTreeComp: InstanceType<typeof RefTreeComp>, parentItems?: any[]) => {
    const bottomResComp = getById(refTreeComp.children.value.getView());
    const currentNodeType = bottomResComp?.type();
    const currentIndex = parentItems ? parentItems.findIndex(item => item === refTreeComp) : -1;

    // 检查是否是文件夹和非文件夹的边界节点
    const isBoundaryNode = parentItems && currentIndex > 0 &&
      parentItems[currentIndex - 1] &&
      getById(parentItems[currentIndex - 1].children.value.getView())?.type() === BottomResTypeEnum.Folder &&
      currentNodeType !== BottomResTypeEnum.Folder;

    const childrenItems = refTreeComp.children.items
      ?.getView()
      .map((i) => convertRefTree(i as InstanceType<typeof RefTreeComp>, refTreeComp.children.items?.getView()))
      .filter((i): i is DraggableTreeNode<BottomResComp> => !!i);
    const node: DraggableTreeNode<BottomResComp> = {
      id: bottomResComp?.id(),
      canDropBefore: (source) => {
        if (isBoundaryNode && source?.type() === BottomResTypeEnum.Folder) {
          return true;
        }
        if (currentNodeType === BottomResTypeEnum.Folder) {
          return source?.type() === BottomResTypeEnum.Folder;
        }
        return source?.type() !== BottomResTypeEnum.Folder;
      },
      canDropAfter: (source) => {
        if (
          currentNodeType !== BottomResTypeEnum.Folder &&
          source?.type() === BottomResTypeEnum.Folder
        ) {
          return false;
        }
        return true;
      },
      canDropIn: (source) => {
        if (currentNodeType !== BottomResTypeEnum.Folder) {
          return false;
        }
        if (!source) {
          return true;
        }
        if (source.type() === BottomResTypeEnum.Folder) {
          return false;
        }
        return true;
      },
      items: childrenItems,
      data: bottomResComp,
      addSubItem(value) {
        const pushAction = refTreeComp.children.items.pushAction({ value: value.id() });
        refTreeComp.children.items.dispatch(pushAction);
      },
      deleteItem(index) {
        const deleteAction = refTreeComp.children.items.deleteAction(index);
        refTreeComp.children.items.dispatch(deleteAction);
      },
      addItem(value) {
        const pushAction = refTreeComp.children.items.pushAction({ value: value.id() });
        refTreeComp.children.items.dispatch(pushAction);
      },
      moveItem(from, to) {
        const moveAction = refTreeComp.children.items.arrayMoveAction(from, to);
        refTreeComp.children.items.dispatch(moveAction);
      },
    };

    if (
      search &&
      bottomResComp &&
      !bottomResComp.name().toLowerCase().includes(search.toLowerCase()) &&
      !matchesPinyinSearch(bottomResComp.commentary(), search) &&
      childrenItems.filter(child => !child.hidden).length === 0
    ) {
      node.hidden = true;
    }
    return node;
  }, [getById, search]);

  const node = useMemo(() => convertRefTree(refTreeComp, refTreeComp.children.items?.getView()), [convertRefTree, refTreeComp]);
  const idsInTree = useMemo(() => refTreeComp.getAllValuesInTree(), [refTreeComp]);
  const itemsNotInTree = useMemo(() =>
    items.filter((i) => !idsInTree.includes(i.id())).map((i) => i.id()),
    [items, idsInTree]
  );

  useEffect(() => {
    if (itemsNotInTree.length > 0) {
      itemsNotInTree.forEach((i) => {
        const pushAction = refTreeComp.children.items.pushAction({
          value: i,
          items: [],
        });
        refTreeComp.children.items.dispatch(pushAction);
      });
    }
  }, [itemsNotInTree, refTreeComp.children.items]);

  const handleSearchToggle = useCallback(() => {
    showSearch(!isSearchShow);
    setSearch("");
  }, [isSearchShow]);

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
  }, []);

  const handleCreatePanel = useCallback(() => {
    onOpenCreatePanel();
  }, [onOpenCreatePanel]);

  const positionLineIndent = useCallback((path: number[], dropInAsSub: boolean) => {
    const indent = 2 + (path.length - 1) * 30;
    if (dropInAsSub) {
      return indent + 12;
    }
    return indent;
  }, [])
  const renderItemContent = useCallback((params: DraggableTreeNodeItemRenderProps<BottomResComp>) => {
    const { node, onToggleFold, onDelete: onDeleteTreeItem, ...otherParams } = params;
    const resComp = node.data;
    if (!resComp) {
      return null;
    }
    const id = resComp.id();
    const type = resComp.type();
    return (
      <BottomSidebarItem
        rename={rename}
        setSelectedBottomRes={setSelectedBottomRes}
        checkRename={checkRename}
        selectedBottomResName={selectedBottomResName}
        selectedBottomResType={selectedBottomResType}
        id={id}
        key={id}
        node={node}
        resComp={resComp}
        onToggleFold={onToggleFold}
        onCopy={() => onCopy(type, id)}
        onSelect={() => onSelect(type, id)}
        onDelete={() => {
          if (onDelete(type, id)) {
            onDeleteTreeItem();
          }
        }}
        {...otherParams}
      />
    );
  }
    , [checkRename, onCopy, onDelete, onSelect, rename, selectedBottomResName, selectedBottomResType, setSelectedBottomRes])

  return (
    <Contain style={props.style}>
      <Title>
        <TitleSpan>
          {trans("bottomPanel.title")} ({items.length})
        </TitleSpan>
        <TitleRight>
          <SearchIconBtn onClick={handleSearchToggle} />
          <AddBtn disabled={readOnly} onClick={handleCreatePanel}>
            <AddIcon />
            {trans("newItem")}
          </AddBtn>
        </TitleRight>
      </Title>
      {isSearchShow && (
        <SearchWrapper>
          <Search
            autoFocus
            allowClear
            value={search}
            placeholder={trans("bottomSearch")}
            onChange={handleSearchChange}
            style={{ marginTop: 0, height: 28 }}
          />
        </SearchWrapper>
      )}
      <ScrollBar>
        {items.length > 0 && node ? (
          <div style={{ paddingTop: 4, paddingBottom: 100, overflow: "hidden" }}>
            <DraggableTree<BottomResComp>
              node={node}
              disable={!!search}
              unfoldAll={!!search}
              showSubInDragOverlay={false}
              showDropInPositionLine={false}
              showPositionLineDot
              positionLineDotDiameter={4}
              positionLineHeight={1}
              itemHeight={25}
              positionLineIndent={positionLineIndent}
              renderItemContent={renderItemContent}
            />
          </div>
        ) : (
          !readOnly && <EmptyQueryList newColumn={onOpenCreatePanel} />
        )}
      </ScrollBar>
    </Contain>
  );
}

const HighlightBorder = styled.div<{ $active: boolean; $foldable: boolean; $level: number }>`
  flex: 1;
  display: flex;
  padding-left: ${(props) => props.$level * 20 + (props.$foldable ? 0 : 14)}px;
  border-radius: 4px;
  border: 1px solid ${(props) => (props.$active ? BorderActiveColor : "transparent")};
  align-items: center;
  justify-content: center;
`;

interface ColumnDivProps {
  $color?: boolean;
  $isOverlay: boolean;
}

const ColumnDiv = styled.div<ColumnDivProps>`
  width: 100%;
  height: 25px;
  display: flex;
  user-select: none;
  padding-left: 2px;
  padding-right: 15px;
  /* background-color: #ffffff; */
  /* margin: 2px 0; */
  background-color: ${(props) => (props.$isOverlay ? "rgba(255, 255, 255, 0.11)" : "")};

  &&& {
    background-color: ${(props) => (props.$color && !props.$isOverlay ? "#f2f7fc" : null)};
  }

  &:hover {
    background-color: #f2f7fc80;
    cursor: pointer;
  }

  .taco-edit-text-wrapper {
    width: 100%;
    height: 21px;
    line-height: 21px;
    color: #222222;
    margin-left: 0;
    font-size: 13px;
    padding-left: 0;

    &:hover {
      background-color: transparent;
    }
  }

  .taco-edit-text-input {
    width: 100%;
    height: 21px;
    line-height: 21px;
    color: #222222;
    margin-left: 0;
    font-size: 13px;
    background-color: #fdfdfd;
    border: 1px solid #3377ff;
    border-radius: 2px;

    &:focus {
      border-color: #3377ff;
      box-shadow: 0 0 0 2px #d6e4ff;
    }
  }
`;
const Icon = styled(PointIcon)`
  width: 16px;
  height: 16px;
  cursor: pointer;
  flex-shrink: 0;
  color: ${NormalMenuIconColor};

  &:hover {
    color: #315efb;
  }
`;

const FoldIconBtn = styled.div`
  width: 12px;
  height: 12px;
  display: flex;
  margin-right: 2px;
`;

const NameAndCommentContainer = styled.div<{ $level: number; $editing: boolean }>`
  display: flex;
  align-items: center;
  flex-grow: 1;
  margin-right: 8px;
  width: ${(props) => props.$editing ? "calc(100% - 62px)" : "auto"};
  min-width: 0;
`;

const NameContainer = styled.div<{ $level: number, $editing: boolean, $isfolder: boolean }>`
  width: ${(props) => (props.$editing || props.$isfolder) ? "100%" : props.$level === 0 ? "110px" : "90px"};
  min-width: ${(props) => props.$level === 0 ? "110px" : "90px"};
  flex-shrink: 0;
`;

const CommentaryContainer = styled.div<{ $editing: boolean }>`
  width: ${(props) => props.$editing ? "100%" : "80px"};
  color: #8b8fa3;
  font-weight: 400!important;
  font-size: 12px;
  margin-left: 8px;
  flex-grow: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  position: relative;
  
  .taco-edit-text-wrapper {
    color: #9195a3 !important;
    font-size: 12px !important;
    width: 100% !important;
    height: 21px !important;
    line-height: 21px !important;
    padding: 0 4px !important;
    justify-content: flex-start !important;
    
    &:hover {
      background-color: transparent !important;
    }
    
    /* 确保编辑图标在右侧固定位置 */
    svg {
      position: absolute !important;
      right: 4px !important;
      top: 50% !important;
      transform: translateY(-50%) !important;
      margin-left: auto !important;
    }
    
    /* 当文本为空时显示占位符样式 */
    &:empty::before {
      content: "添加注释...";
      color: #c0c4cc;
      font-style: italic;
    }
  }
  
  .taco-edit-text-input {
    color: #9195a3 !important;
    font-size: 12px !important;
    background-color: #fdfdfd !important;
    border: 1px solid #3377ff !important;
    border-radius: 2px !important;
    width: 100% !important;
    height: 21px !important;
    line-height: 21px !important;
    padding: 0 4px !important;
    
    &:focus {
      border-color: #9195a3 !important;
      box-shadow: 0 0 0 2px #d6e4ff !important;
    }
    
    &::placeholder {
      color: #c0c4cc;
      font-style: italic;
    }
  }
`;

interface BottomSidebarItemProps extends DraggableTreeNodeItemRenderProps {
  id: string;
  resComp: BottomResComp;
  onCopy: () => void;
  onSelect: () => void;
  onDelete: () => void;
  onToggleFold: () => void;
  selectedBottomResName: string;
  selectedBottomResType?: BottomResTypeEnum;
  rename: (oldName: string, newName: string) => boolean;
  setSelectedBottomRes: (name: string, type: BottomResTypeEnum) => void;
  checkRename: (name: string, value: string) => string;
}

const BottomSidebarItem = memo((props: BottomSidebarItemProps) => {
  const {
    id,
    resComp,
    isOver,
    isOverlay,
    path,
    isFolded,
    onDelete,
    onCopy,
    onSelect,
    onToggleFold,
    selectedBottomResName,
    selectedBottomResType,
    rename,
    setSelectedBottomRes,
    checkRename,
  } = props;
  const [error, setError] = useState<string | undefined>(undefined);
  const [editing, setEditing] = useState(false);
  const [editingCommentary, setEditingCommentary] = useState(false);
  const [isHover, setIsHover] = useState(false);
  const readOnly = useSelector(showAppSnapshotSelector);
  const level = path.length - 1;
  const type = useMemo(() => resComp.type(), [resComp]);
  const name = resComp.name();
  const commentary = resComp.commentary();
  const icon = resComp.icon();
  const isSelected = type === selectedBottomResType && id === selectedBottomResName;
  const isFolder = useMemo(() => type === BottomResTypeEnum.Folder, [type]);

  const handleFinishRename = useCallback((value: string) => {
    let success = false;
    let compId = name;
    value = value.trim().slice(0, 25);
    if (resComp.rename) {
      compId = resComp.rename(value);
      success = !!compId;
    } else {
      compId = name;
      success = rename(name, value);
    }
    if (success) {
      setSelectedBottomRes(compId, type);
      setError(undefined);
    }
  }, [resComp, name, rename, setSelectedBottomRes, type]);

  const handleNameChange = useCallback((value: string) => {
    let err = "";
    if (resComp.checkName) {
      err = resComp.checkName(value);
    } else {
      err = checkRename(name, value);
    }
    setError(err);
  }, [resComp, name, checkRename]);

  const handleClickItem = useCallback(() => {
    if (isFolder) {
      onToggleFold();
    }
    onSelect();
  }, [isFolder, onToggleFold, onSelect]);

  const handleEditStateChange = useCallback((editing: boolean) => {
    setEditing(editing);
  }, []);

  const handleFinishCommentaryEdit = useCallback((value: string) => {
    if (resComp.changeCommentary) {
      resComp.changeCommentary(value.trim().slice(0, 25));
    }
    setEditingCommentary(false);
  }, [resComp]);

  const handleCommentaryEditStateChange = useCallback((editing: boolean) => {
    setEditingCommentary(editing);
  }, []);

  return (
    <ColumnDiv onClick={handleClickItem}
      $color={isSelected}
      $isOverlay={isOverlay}
      onMouseEnter={() => setIsHover(true)}
      onMouseLeave={() => setIsHover(false)}>
      <HighlightBorder $active={isOver && isFolder} $level={level} $foldable={isFolder}>
        {isFolder && <FoldIconBtn>{!isFolded ? <FoldedIcon /> : <UnfoldIcon />}</FoldIconBtn>}
        {icon}
        <NameAndCommentContainer $level={level} $editing={editing}>
          {!editingCommentary && (
            <NameContainer $level={level} $editing={editing} $isfolder={isFolder}>
              <EditText
                text={name}
                forceClickIcon={isFolder}
                disabled={!isSelected || readOnly || isOverlay}
                onFinish={handleFinishRename}
                onChange={handleNameChange}
                onEditStateChange={handleEditStateChange}
                disableHoverIcon={!isSelected}
              />
              <PopupCard
                editorFocus={!!error && editing}
                title={error ? trans("error") : ""}
                content={error}
                hasError={!!error}
              />
            </NameContainer>)}
          {!isFolder && !editing && (
            <CommentaryContainer $editing={editingCommentary}>
              <EditText
                text={commentary || ''}
                forceClickIcon={false}
                disabled={!isSelected || readOnly || isOverlay}
                onFinish={handleFinishCommentaryEdit}
                onEditStateChange={handleCommentaryEditStateChange}
                editing={editingCommentary}
                disableHoverIcon={!isSelected}
              />
            </CommentaryContainer>
          )}
        </NameAndCommentContainer>
        {!readOnly && !isOverlay && (isHover || editing || editingCommentary) && (
          <EditPopover copy={!isFolder ? onCopy : undefined} del={onDelete}>
            <Icon tabIndex={-1} />
          </EditPopover>
        )}
      </HighlightBorder>
    </ColumnDiv>
  );
});
BottomSidebarItem.displayName = "BottomSidebarItem";

/* Empty list */
const NolistDiv = styled.div`
  margin: 16px 16px 0 16px;
  width: 100%;
  min-height: 68px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border: 1px dashed #d7d9e0;
  border-radius: 4px;
  overflow: auto;
  min-width: 140px;
`;

export const EmptyQueryList = (props: { newColumn: () => void }) => (
  <div style={{ display: "flex" }}>
    <NolistDiv>
      <div style={{ color: "#b8b9bf" }}>{trans("query.noQueries")}</div>
      <span
        style={{ color: "#4965f2", cursor: "pointer", margin: "0 4px" }}
        onClick={props.newColumn}
      >
        {trans("newItem")}
      </span>
    </NolistDiv>
  </div>
);
