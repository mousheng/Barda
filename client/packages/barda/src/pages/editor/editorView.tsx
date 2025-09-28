import { draggingUtils } from "@barda/layout/draggingUtils";
import { Menu } from "antd";
import { MenuProps } from "antd/es/menu";
import { PreloadComp } from "comps/comps/preLoadComp";
import UIComp from "comps/comps/uiComp";
import { EditorContext } from "comps/editorState";
import { Layers } from "constants/Layers";
import { TopHeaderHeight } from "constants/style";
import Header, { PanelStatus, TogglePanel } from "pages/common/header";
import { PreviewHeader } from "pages/common/previewHeader";
import {
  Body,
  EditorContainer,
  EditorContainerWithViewMode,
  Height100Div,
  LeftPanel,
  MiddlePanel,
} from "pages/common/styledComponent";
import {
  CustomShortcutWrapper,
  EditorGlobalHotKeys,
  EditorHotKeys,
} from "pages/editor/editorHotKeys";
import RightPanel from "pages/editor/right/RightPanel";
import EditorTutorials from "pages/tutorials/editorTutorials";
import { editorContentClassName, UserGuideLocationState } from "pages/tutorials/tutorialsConstant";
import { memo, useCallback, useContext, useLayoutEffect, useMemo, useRef, useState } from "react";
import { Helmet } from "react-helmet";
import { useSelector } from "react-redux";
import { useLocation } from "react-router-dom";
import { currentApplication } from "redux/selectors/applicationSelector";
import { showAppSnapshotSelector } from "redux/selectors/appSnapshotSelector";
import styled from "styled-components";
import { ExternalEditorContext } from "util/context/ExternalEditorContext";
import { useTemplateViewMode } from "util/hooks";
import { DefaultPanelStatus, getPanelStatus, savePanelStatus } from "util/localStorageUtil";
import Bottom from "./bottom/BottomPanel";
import { LeftSideMenu, SiderKey } from "./left/leftSideMenu";
import { SettingsPanel } from "./left/settingsPanel";
import { LeftContent } from "./LeftContent";

const HookCompContainer = styled.div`
  pointer-events: none;
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  contain: paint;
  z-index: ${Layers.hooksCompContainer};
`;

const ViewBody = styled.div<{ $hideBodyHeader?: boolean; $height?: number }>`
  height: ${(props) => `calc(${props.$height ? props.$height + "px" : "100vh"
    } - env(safe-area-inset-bottom) -
      ${props.$hideBodyHeader ? "0px" : TopHeaderHeight}
  )`};
`;

export const EditorWrapper = styled.div`
  overflow: auto;
  position: relative;
  flex: 1 1 0;
`;

interface EditorViewProps {
  uiComp: InstanceType<typeof UIComp>;
  preloadComp: InstanceType<typeof PreloadComp>;
}

const MemoizedMenu = memo(({ items, selectedKeys, onClick, disabled, defaultSelectedKeys }: MenuProps) => {
  return (
    <Menu
      theme="dark"
      mode="inline"
      defaultSelectedKeys={defaultSelectedKeys}
      selectedKeys={selectedKeys}
      items={items}
      disabled={disabled}
      onClick={onClick}
    />
  );
});

MemoizedMenu.displayName = "MemoizedMenu";

function EditorView(props: EditorViewProps) {
  const { uiComp } = props;
  const editorState = useContext(EditorContext);
  const { readOnly, hideHeader } = useContext(ExternalEditorContext);
  const application = useSelector(currentApplication);
  const locationState = useLocation<UserGuideLocationState>().state;
  const showNewUserGuide = locationState?.showNewUserGuide;
  const showAppSnapshot = useSelector(showAppSnapshotSelector);
  const [showShortcutList, setShowShortcutList] = useState(false);
  const toggleShortcutList = useCallback(
    () => setShowShortcutList(!showShortcutList),
    [showShortcutList]
  );
  const [menuKey, setMenuKey] = useState<string>(SiderKey.State);
  const containerRef = useRef(null);
  const [height, setHeight] = useState<number>();

  const [panelStatus, setPanelStatus] = useState(() => {
    return showNewUserGuide ? DefaultPanelStatus : getPanelStatus();
  });
  const [prePanelStatus, setPrePanelStatus] = useState<PanelStatus>(DefaultPanelStatus);
  const defaultSelectedKeys = useRef([SiderKey.State]);
  const selectedKeys = useMemo(() => panelStatus.left ? [menuKey] : [""], [menuKey, panelStatus.left])


  const togglePanel: TogglePanel = useCallback(
    (key) => {
      let newPanelStatus;
      if (key) {
        newPanelStatus = Object.assign({}, panelStatus);
        newPanelStatus[key] = !panelStatus[key];
      } else {
        if (Object.values(panelStatus).some((value) => value)) {
          setPrePanelStatus(panelStatus);
          newPanelStatus = { left: false, bottom: false, right: false };
        } else {
          newPanelStatus = prePanelStatus;
        }
      }
      setPanelStatus(newPanelStatus);
      savePanelStatus(newPanelStatus);
    },
    [panelStatus, prePanelStatus]
  );

  const onCompDrag = useCallback(
    (dragCompKey: string) => {
      editorState.setDraggingCompType(dragCompKey);
    },
    [editorState.draggingCompType]
  );
  const setShowPropertyPane = useCallback(
    (tabKey: string) => {
      editorState.setShowPropertyPane(tabKey === "property");
    },
    [editorState.showPropertyPane]
  );

  const hookCompViews = useMemo(() => {
    return Object.keys(editorState.getHooksComp().children).map((key) => (
      // use appId as key, remount hook comp when app change. fix hookStateComp empty value
      <div key={key + "-" + application?.applicationId}>
        {editorState.getHooksComp().children[key].getView()}
      </div>
    ));
  }, [application?.applicationId, editorState.rootComp]);

  useLayoutEffect(() => {
    function updateSize() {
      setHeight(window.innerHeight);
    }

    const eventType = "orientationchange" in window ? "orientationchange" : "resize";
    window.addEventListener(eventType, updateSize);
    updateSize();
    return () => window.removeEventListener(eventType, updateSize);
  }, []);

  const clickMenu = useCallback((params: { key: string }) => {
    let left = true;
    if (panelStatus.left && params.key === menuKey) {
      left = false;
    }
    setPanelStatus({ ...panelStatus, left });
    savePanelStatus({ ...panelStatus, left });
    setMenuKey(params.key);
  }, [menuKey, panelStatus]);

  const hideBodyHeader = useTemplateViewMode() || editorState.getAppSettings()?.hiddenHeader === "hiddenHeader";

  const uiCompView = useMemo(() => {
    if (showAppSnapshot) {
      return (
        <ViewBody $hideBodyHeader={hideBodyHeader} $height={height}>
          <EditorContainer>{uiComp.getView()}</EditorContainer>
        </ViewBody>
      );
    } else {
      return uiComp.getView();
    }
  }, [showAppSnapshot, hideBodyHeader, height, uiComp]);


  if (readOnly && hideHeader) {
    return (
      <CustomShortcutWrapper>
        {uiComp.getView()}
        <div style={{ zIndex: Layers.hooksCompContainer }}>{hookCompViews}</div>
      </CustomShortcutWrapper>
    );
  }

  if (readOnly && !showAppSnapshot) {
    return (
      <CustomShortcutWrapper>
        <Helmet>{application && <title>{application.name}</title>}</Helmet>
        {!hideBodyHeader && <PreviewHeader />}
        <EditorContainerWithViewMode>
          <ViewBody $hideBodyHeader={hideBodyHeader} $height={height}>
            {uiComp.getView()}
          </ViewBody>
          <div style={{ zIndex: Layers.hooksCompContainer }}>{hookCompViews}</div>
        </EditorContainerWithViewMode>
      </CustomShortcutWrapper>
    );
  }
  // history mode, display with the right panel, a little trick
  const showRight = panelStatus.right || showAppSnapshot;
  // let uiCompView;
  // if (showAppSnapshot) {
  //   uiCompView = (
  //     <ViewBody $hideBodyHeader={hideBodyHeader} $height={height}>
  //       <EditorContainer>{uiComp.getView()}</EditorContainer>
  //     </ViewBody>
  //   );
  // } else {
  //   uiCompView = uiComp.getView();
  // }

  const appSettingsComp = editorState.getAppSettingsComp();

  return (
    <Height100Div
      onDragEnd={(e: any) => {
        editorState.setDragging(false);
        draggingUtils.clearData();
      }}
    >
      <Header togglePanel={togglePanel} panelStatus={panelStatus} />
      <Helmet>{application && <title>{application.name}</title>}</Helmet>
      {showNewUserGuide && <EditorTutorials />}
      <EditorGlobalHotKeys
        disabled={readOnly}
        togglePanel={togglePanel}
        panelStatus={panelStatus}
        toggleShortcutList={toggleShortcutList}
      >
        <Body>
          <LeftSideMenu
            selectedKeys={selectedKeys}
            disabled={showAppSnapshot}
            onClick={clickMenu}
            defaultSelectedKeys={defaultSelectedKeys.current}
            showShortcutList={showShortcutList}
            setShowShortcutList={setShowShortcutList}
          />

          {panelStatus.left && (
            <LeftPanel menuKey={menuKey}>
              {menuKey === SiderKey.State && <LeftContent uiComp={uiComp} togglePanel={togglePanel} />}
              {menuKey === SiderKey.Setting && (
                <SettingsPanel
                  containerRef={containerRef}
                  panelStatus={panelStatus}
                  setPanelStatus={setPanelStatus}
                  application={application}
                  appSettingsComp={appSettingsComp}
                  preloadComp={props.preloadComp}
                />
              )}
            </LeftPanel>
          )}
          <MiddlePanel>
            <EditorWrapper className={editorContentClassName}>
              <EditorHotKeys disabled={readOnly}>
                <EditorContainerWithViewMode>
                  {uiCompView}
                  <HookCompContainer>{hookCompViews}</HookCompContainer>
                </EditorContainerWithViewMode>
              </EditorHotKeys>
            </EditorWrapper>
            {panelStatus.bottom && <Bottom />}
          </MiddlePanel>
          {showRight && (
            <RightPanel
              uiComp={uiComp}
              onCompDrag={onCompDrag}
              showPropertyPane={editorState.showPropertyPane}
              onTabChange={setShowPropertyPane}
            />
          )}
        </Body>
      </EditorGlobalHotKeys>
    </Height100Div >
  );
}

export default EditorView;
