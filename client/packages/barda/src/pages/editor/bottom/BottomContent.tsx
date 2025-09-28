import RefTreeComp from "@barda/comps/comps/refTreeComp";
import { messageInstance, NofileIcon } from "barda-design";
import { MetaDataContext } from "base/codeEditor/codeEditorTypes";
import { ResCreatePanel } from "components/ResCreatePanel";
import { CompNameContext, EditorContext } from "comps/editorState";
import { trans } from "i18n";
import { editorBottomClassName } from "pages/tutorials/tutorialsConstant";
import { memo, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { getDataSource } from "redux/selectors/datasourceSelectors";
import styled, { css } from "styled-components";
import { BottomResComp, BottomResTypeEnum } from "types/bottomRes";
import { useMetaData } from "util/hooks";
import BottomMetaDrawer from "./BottomMetaDrawer";
import { BottomSidebar } from "./BottomSidebar";
import { EmptyTab } from "./BottomTabs";

const Container = styled.div`
  width: 100%;
  height: 100%;
  background: #ffffff;
  display: flex;
`;

const Left = styled.div`
  position: relative;
  width: calc(30% - 1px);
  min-width: 248px;
  max-width: 296px;
  height: 100%;
  float: left;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
`;
const Drag = styled.div`
  width: 1px;
  height: 100%;
  background-color: #e1e3eb;
  position: relative;
  flex-shrink: 0;
`;
const Right = styled.div`
  position: relative;
  min-width: 0;
  height: 100%;
  overflow-y: hidden;
  overflow-x: auto;
  flex-grow: 1;
`;

export function BottomSkeleton() {
  return (
    <Container>
      <Left></Left>
      <Drag />
      <Right></Right>
    </Container>
  );
}

export const BottomContent = () => {
  const editorState = useContext(EditorContext);
  const datasourceInfos = useSelector(getDataSource);
  const selectedComp = editorState.selectedBottomResComp();
  const [isCreatePanelShow, showCreatePanel] = useState(false);
  const isFolderSelected = editorState.selectedBottomResType === BottomResTypeEnum.Folder;

  const refTreeComp = useMemo(() => editorState.rootComp.children.refTree,
    [editorState.rootComp.children.refTree]);

  const folderItemsMemo = useMemo(() => editorState.getFoldersComp().getView(),
    [editorState.rootComp.children.folders]);

  const bottomResItems = useMemo(() => [
    ...editorState.getQueriesComp().getView(),
    ...editorState.getTempStatesComp().getView(),
    ...editorState.getTransformersComp().getView(),
    ...folderItemsMemo,
    ...editorState.getDataRespondersComp().getView()
  ] as unknown as BottomResComp[],
    [editorState.rootComp.children.queries,
    editorState.rootComp.children.tempStates,
    editorState.rootComp.children.transformers,
    editorState.rootComp.children.dataResponders,
      folderItemsMemo
    ])

  const bottomLeftOnOpenCreatePanel = useCallback(() => {
    showCreatePanel(true);
  }, [showCreatePanel]);

  const recentlyUsed = useMemo(() => bottomResItems.reverse().map((i) => {
    if (i.type() === BottomResTypeEnum.Query) {
      const dsi = datasourceInfos.find(
        (info) => info.datasource.id === (i as any).children.datasourceId?.getView()
      );
      return dsi?.datasource || (i as any).children.compType?.getView();
    }
    return i.type();
  }),
    [bottomResItems, datasourceInfos]
  );


  const handleCopy = useCallback((type: BottomResTypeEnum, name: string) => {
    // getBottomResListComp 需读取五种类型资源，所以需要将五种类型资源作为依赖
    const listComp = editorState.getBottomResListComp(type);
    listComp.copy(editorState, name);
  },
    [bottomResItems]);

  const handleDelete = useCallback((type: BottomResTypeEnum, name: string) => {
    const listComp = editorState.getBottomResListComp(type);
    if (type === BottomResTypeEnum.Folder && refTreeComp.hasChildren(name)) {
      messageInstance.error(trans("query.folderNotEmpty"));
      return false;
    }
    listComp.delete(name);
    return true;
  },
    [bottomResItems, refTreeComp]);

  const handleSelect = useCallback((type: BottomResTypeEnum, name: string) => {
    const listComp = editorState.getBottomResListComp(type);
    listComp.select(editorState, name);
  },
    [bottomResItems]);

  useEffect(() => {
    editorState.selectedBottomResName && showCreatePanel(false);
  }, [editorState.selectedBottomResName, showCreatePanel]);

  // keep reference unchanged when metaData unchange, avoid re-configure when auto-completion changes
  const selectedDatasourceId = useMemo(() => editorState.selectedQueryComp()?.children.datasourceId.getView() || "",
    [editorState.rootComp.children.queries]);
  const selectedQueryType = useMemo(() => editorState.selectedQueryComp()?.children.compType.getView() || "",
    [editorState.rootComp.children.queries]);

  const rename = useCallback((oldName: string, newName: string) =>
    editorState.rename(oldName, newName),
    [bottomResItems]);
  const checkRename = useCallback((oldName: string, newName: string) =>
    editorState.checkRename(oldName, newName),
    [bottomResItems]);
  const setSelectedBottomRes = useCallback((name: string, type: BottomResTypeEnum) => {
    editorState.setSelectedBottomRes(name, type);
  }, [bottomResItems]);

  const datasource = useMemo(() => datasourceInfos.map((i) => i.datasource).filter((d) => d.creationSource !== 2), [datasourceInfos])
  const handleAdd = useCallback((type: BottomResTypeEnum, extraInfo?: any) => {
    const listComp = editorState.getBottomResListComp(type);
    const id = listComp.add(editorState, extraInfo);
    showCreatePanel(false);
    const isFolder = type === BottomResTypeEnum.Folder;
    const parent = isFolderSelected && !isFolder ? editorState.selectedBottomResName : "";
    const index = isFolder ? folderItemsMemo.length : undefined;
    refTreeComp.appendRef(parent, id, index);
  }, [refTreeComp, isFolderSelected, folderItemsMemo, editorState.selectedBottomResName, bottomResItems]);
  const ResCreatePanelOnClose = useCallback(() => showCreatePanel(false), []);
  const selectedCompPanel = useMemo(() => selectedComp ? selectedComp.getPropertyView() : EmptyQuery, [selectedComp])

  const metaData = useMetaData(selectedDatasourceId);
  return (
    <Container className={editorBottomClassName}>
      <Left>
        <BottomLeft
          items={bottomResItems}
          onOpenCreatePanel={bottomLeftOnOpenCreatePanel}
          onDelete={handleDelete}
          onCopy={handleCopy}
          onSelect={handleSelect}
          selectedDataSourceId={selectedDatasourceId}
          selectedQueryType={selectedQueryType}
          refTree={refTreeComp}
          selectedBottomResName={editorState.selectedBottomResName}
          selectedBottomResType={editorState.selectedBottomResType}
          rename={rename}
          setSelectedBottomRes={setSelectedBottomRes}
          checkRename={checkRename}
        />
      </Left>
      <Drag />
      <Right>
        <div style={{ width: "100%", height: "100%", minWidth: "480px" }}>
          <MetaDataContext.Provider value={metaData}>
            <CompNameContext.Provider
              value={editorState.selectedQueryComp()?.children.name.getView() || ""}
            >
              {selectedCompPanel}
            </CompNameContext.Provider>
          </MetaDataContext.Provider>
          {(isCreatePanelShow || isFolderSelected) && (
            <ResCreatePanel
              recentlyUsed={recentlyUsed}
              datasource={datasource}
              onSelect={handleAdd}
              onClose={ResCreatePanelOnClose}
            />
          )}
        </div>
      </Right>
    </Container>
  );
};

interface BottomLeftProps {
  selectedDataSourceId?: string;
  selectedQueryType?: string;
  selectedBottomResName: string;
  selectedBottomResType?: BottomResTypeEnum;
  refTree: RefTreeComp;
  items: BottomResComp[];
  onOpenCreatePanel: () => void;
  onCopy: (type: BottomResTypeEnum, name: string) => void;
  onDelete: (type: BottomResTypeEnum, name: string) => boolean;
  onSelect: (type: BottomResTypeEnum, name: string) => void;
  rename: (oldName: string, newName: string) => boolean;
  setSelectedBottomRes: (name: string, type: BottomResTypeEnum) => void;
  checkRename: (name: string, value: string) => string;
}

const BottomLeft = memo((props: BottomLeftProps) => {
  const { items, selectedDataSourceId, refTree, selectedQueryType, selectedBottomResName, selectedBottomResType,
    onOpenCreatePanel, onCopy, onSelect, onDelete, rename, setSelectedBottomRes, checkRename } = props;

  return (
    <>
      <BottomSidebar
        refTreeComp={refTree}
        dataSourceId={selectedDataSourceId}
        items={items}
        onCopy={onCopy}
        onDelete={onDelete}
        onSelect={onSelect}
        onOpenCreatePanel={onOpenCreatePanel}
        selectedBottomResName={selectedBottomResName}
        selectedBottomResType={selectedBottomResType}
        rename={rename}
        setSelectedBottomRes={setSelectedBottomRes}
        checkRename={checkRename}
      />
      {selectedDataSourceId && selectedQueryType && (
        <BottomMetaDrawer dataSourceId={selectedDataSourceId} queryType={selectedQueryType} />
      )}
    </>
  );
});
BottomLeft.displayName = "BottomLeft";

const labelCss: any = css`
  user-select: text;

  font-size: 13px;
  line-height: 13px;
  cursor: text;
`;

const PicDiv = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  transform: translateY(-48px);
`;
const EmptyLabel = styled.span`
  ${labelCss};
  display: block;
  margin-top: 10px;
  color: #b8b9bf;
  text-align: center;
`;
const EmptyDiv = styled.div`
  height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
`;
export const EmptyQuery = (
  <>
    {EmptyTab}
    <EmptyDiv>
      <PicDiv>
        <NofileIcon />
        <EmptyLabel>{trans("bottomPanel.noSelectedQuery")}</EmptyLabel>
      </PicDiv>
    </EmptyDiv>
  </>
);

export const EmptyQueryWithoutTab = (
  <EmptyDiv>
    <PicDiv>
      <NofileIcon />
      <EmptyLabel>{trans("bottomPanel.noSelectedQuery")}</EmptyLabel>
    </PicDiv>
  </EmptyDiv>
);
