import { useState } from "react";
import { Menu, Tooltip } from "antd";
import { CompPlayground } from "./CompPlayground";
import styled, { css } from "styled-components";
import { UICompLayoutInfo } from "comps/uiCompRegistry";
import { Left, Middle, Right } from "barda-design";
import { trans } from "i18n";

const Container = styled.div`
  display: flex;
  flex-direction: column;
`;

const Header = styled.div`
  height: 48px;
  border-bottom: 1px solid #e1e3eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  .pkg-name {
    line-height: 48px;
  }
  .name {
    font-size: 14px;
    font-weight: bold;
  }
  .version {
    margin-left: 4px;
    font-size: 12px;
    color: #333;
  }
`;

const HeaderLeft = styled.div`
  flex: 1;
`;

const HeaderCenter = styled.div`
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
`;

const HeaderRight = styled.div`
  flex: 1;
`;

const IconCss = css<{ $show: boolean }>`
  &:hover {
    background-color: #8b8fa34c;
  }

  & g g {
    stroke: ${(props) => (props.$show ? "#dddddd" : "#dddddd65")};
  }

  &:hover g g {
    stroke: #ffffff;
  }

  cursor: pointer;
`;

const LeftIcon = styled(Left)`
  ${IconCss as any}
`;

const MiddleIcon = styled(Middle) <{ $show: boolean }>`
  ${IconCss}
  & g line {
    stroke: ${(props) => (props.$show ? "#dddddd" : "#dddddd65")};
  }

  &:hover g line {
    stroke: #ffffff;
  }

  & g rect {
    stroke: ${(props) => (props.$show ? "#dddddd" : "#dddddd65")};
  }

  &:hover g rect {
    stroke: #ffffff;
  }
`;

const RightIcon = styled(Right)`
  ${IconCss as any}
`;

const IconRadius = styled.div`
  width: 28px;
  height: 28px;
  border-radius: 4px;
  overflow: hidden;
`;

const ControlButtons = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const Main = styled.div`
  display: flex;
  flex-direction: row;
  min-width: 0;
`;

const SideBar = styled.div<{ $show: boolean }>`
  flex: ${(props) => (props.$show ? "0 0 200px" : "0 0 0")};
  min-width: ${(props) => (props.$show ? "200px" : "0")};
  background-color: #fff;
  border-right: ${(props) => (props.$show ? "1px solid #e1e3eb" : "none")};
  overflow: auto;
  height: calc(100vh - 48px);
  display: ${(props) => (props.$show ? "block" : "none")};
  .ant-menu {
    border-right: 0;
  }
`;

const Content = styled.div`
  flex: 1;
  min-width: 0;
  height: calc(100vh - 48px);
  overflow: auto;
`;

export interface CompMeta {
  name: string;
  layoutInfo?: UICompLayoutInfo;
}

export interface CompIDEProps {
  compMap: Record<string, any>;
  compMeta: Record<string, CompMeta>;
  packageName: string;
  packageVersion: string;
}

export function CompIDE(props: CompIDEProps) {
  const { compMap, compMeta, packageName, packageVersion } = props;
  const [currentCompName, setCurrentCompName] = useState<string>(Object.keys(compMeta)[0]);
  const layoutInfo = compMeta[currentCompName]?.layoutInfo || { w: 5, h: 5 };
  const [showSidebar, setShowSidebar] = useState(true);
  const [showJsonView, setShowJsonView] = useState(true);
  const [showPropertyPanel, setShowPropertyPanel] = useState(true);

  const items = Object.keys(compMeta).map((i) => ({
    key: i,
    label: i,
  }));

  const togglePanel = (panel: "sidebar" | "jsonView" | "propertyPanel") => {
    switch (panel) {
      case "sidebar":
        setShowSidebar(!showSidebar);
        break;
      case "jsonView":
        setShowJsonView(!showJsonView);
        break;
      case "propertyPanel":
        setShowPropertyPanel(!showPropertyPanel);
        break;
    }
  };

  return (
    <Container>
      <Header>
        <HeaderLeft>
          <div className="pkg-name">
            <span className="name">{packageName}</span>
            <span className="version"> - Version: {packageVersion}</span>
          </div>
        </HeaderLeft>
        <HeaderCenter>
          <ControlButtons>
            <Tooltip title={trans("header.left")}>
              <IconRadius>
                <LeftIcon onClick={() => togglePanel("sidebar")} $show={showSidebar} />
              </IconRadius>
            </Tooltip>
            <Tooltip title={trans("playground.data")}>
              <IconRadius>
                <MiddleIcon onClick={() => togglePanel("jsonView")} $show={showJsonView} />
              </IconRadius>
            </Tooltip>
            <Tooltip title={trans("playground.property")}>
              <IconRadius>
                <RightIcon onClick={() => togglePanel("propertyPanel")} $show={showPropertyPanel} />
              </IconRadius>
            </Tooltip>
          </ControlButtons>
        </HeaderCenter>
        <HeaderRight />
      </Header>
      <Main>
        <SideBar $show={showSidebar}>
          <Menu
            onSelect={(k) => setCurrentCompName(k.selectedKeys[0])}
            selectedKeys={[currentCompName]}
            items={items}
            mode="inline"
          />
        </SideBar>
        <Content>
          <CompPlayground
            compFactory={compMap[currentCompName]}
            layoutInfo={layoutInfo}
            showJsonView={showJsonView}
            showPropertyPanel={showPropertyPanel}
          />
        </Content>
      </Main>
    </Container>
  );
}
