import { Tabs } from "antd";
import styled from "styled-components";

export const RightPanelTabs = styled(Tabs) <{ $zIndex?: number }>`
  border: 1px solid #d0d3dd;
  width: 312px;
  display: flex;
  z-index: ${props => props.$zIndex || 0};
  .ant-tabs-content {
    height: 100%;
    overflow: hidden;
    background-color: #ffffff;
  }
  .ant-tabs-tabpane {
    height: 100%;
  }
  .ant-tabs-content::-webkit-scrollbar {
    width: 6px;
    right: 13px;
  }
  .ant-tabs-content::-webkit-scrollbar-thumb {
    background: #d0d3dd;
    border-radius: 3px;
  }
  .ant-tabs-content::-webkit-scrollbar-track {
    background: #f1f1f1;
  }
  .ant-tabs-nav {
    height: 32px;
    margin-bottom: 0px;
    .ant-tabs-nav-list {
      margin-left: 17px;
    }
  }
`;