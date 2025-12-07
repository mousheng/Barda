import { DoubleLeftOutlined } from "@ant-design/icons";
import { trans } from "@barda/i18n";
import { PanelStatus } from "@barda/pages/common/header";
import { Anchor, Col, Row } from "antd";
import { AppSettingsModal } from "comps/comps/appSettingsComp";
import { PreloadComp } from "comps/comps/preLoadComp";
import UIComp, { UiLayoutType } from "comps/comps/uiComp";
import { ApplicationMeta } from "constants/applicationConstants";
import { useSelector } from "react-redux";
import { getDefaultTheme, getThemeList } from "redux/selectors/commonSettingSelectors";
import styled from "styled-components";
import { isAggregationApp } from "util/appUtils";
import { CloseButton } from "../codeEditorPanel";

const SettingsDiv = styled.div`
  height: 100%;
  display: flex;
  flex-direction: column;
  :where(.css-dev-only-do-not-override-98ntnt).ant-anchor-wrapper .ant-anchor .ant-anchor-link-title {
    color: #222;
    font-size: 13px;
  }
  :where(.css-dev-only-do-not-override-98ntnt).ant-anchor-wrapper
    .ant-anchor
    .ant-anchor-link-active
    > .ant-anchor-link-title {
    color: #1677ff;
  }

  .ant-anchor {
    top: 12px;
  }
`;

const SettingsHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px 0px 16px;
  border-bottom: 1px solid #f1f1f1;
`;

const H3Title = styled.h3`
  color: #8b8fa3;
  font-weight: 600;
`;

const SettingsContentDiv = styled.div`
  display: flex;
  flex-direction: column;
  flex-wrap: nowrap;
  height: 100%;
  margin-right: 1px;
  overflow: auto;
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: #d0d3dd;
    border-radius: 3px;
  }
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
  }
  .ant-divider {
    margin: 16px 0;
    border-color: #e1e3eb;
  }
`;

export enum anchorItemKey {
  PageSettings = "PageSettings",
  CSS = "CSS",
  PreLoadJS = "PreLoadJS",
  Lib = "Lib",
  Shortcuts = "Shortcuts",
  Theme = "Theme",
}

const subItems = [
  {
    key: anchorItemKey.PageSettings,
    href: "#" + anchorItemKey.PageSettings,
    title: trans("appSetting.pageSettings"),
  },
  {
    key: anchorItemKey.Shortcuts,
    href: "#" + anchorItemKey.Shortcuts,
    title: trans("customShortcut.shortcut"),
  },
  {
    key: anchorItemKey.Theme,
    href: "#" + anchorItemKey.Theme,
    title: trans("settings.theme"),
  },
  {
    key: anchorItemKey.PreLoadJS,
    href: "#" + anchorItemKey.PreLoadJS,
    title: "JS",
  },
  {
    key: anchorItemKey.CSS,
    href: "#" + anchorItemKey.CSS,
    title: "CSS",
  },
  {
    key: anchorItemKey.Lib,
    href: "#" + anchorItemKey.Lib,
    title: trans("advanced.preloadLibsTitle"),
  },
];

interface SettingsPanelProps {
  containerRef: React.RefObject<HTMLDivElement>;
  panelStatus: PanelStatus;
  setPanelStatus: (status: any) => void;
  application: ApplicationMeta | undefined;
  appSettingsComp: any;
  preloadComp: InstanceType<typeof PreloadComp>;
  uiComp?: InstanceType<typeof UIComp>;
}

export function SettingsPanel({
  containerRef,
  panelStatus,
  setPanelStatus,
  application,
  appSettingsComp,
  preloadComp,
  uiComp,
}: SettingsPanelProps) {
  const isNavLayout = isAggregationApp(uiComp?.children.compType.getView() as UiLayoutType);
  const anchorItems = isNavLayout ? subItems.filter(item => item.key !== anchorItemKey.Theme) : subItems;
  const themeList = useSelector(getThemeList) || [];
  const defaultTheme = (useSelector(getDefaultTheme) || "").toString();

  return (
    <SettingsDiv>
      <SettingsHeader>
        <H3Title>{trans("leftPanel.settingsTab")}</H3Title>
        <CloseButton onClick={() => setPanelStatus({ ...panelStatus, left: false })}>
          <DoubleLeftOutlined />
        </CloseButton>
      </SettingsHeader>
      <SettingsContentDiv ref={containerRef}>
        <Row>
          <Col span={4}>
            <Anchor
              getContainer={() => containerRef.current!}
              onClick={(e) => e.preventDefault()}
              items={anchorItems}
            />
          </Col>
          <Col span={20}>
            <div id={anchorItemKey.PageSettings}>
              {application &&
                  <>
                    <AppSettingsModal
                      {...appSettingsComp.children}
                      themeList={themeList}
                      defaultTheme={defaultTheme}
                      hideNavOptions={isNavLayout}
                    />
                    {
                      <>
                        {preloadComp.getPropertyView()}
                        {preloadComp.getJSLibraryPropertyView()}
                      </>
                    }
                  </>
                }
            </div>
          </Col>
        </Row>
      </SettingsContentDiv>
    </SettingsDiv>
  );
}
