import { trans } from "i18n/design";
import styled from "styled-components";
import { BigButtonStyle } from "./button";
import Tree from "antd/es/tree"
import type { TreeProps } from "antd/lib";

const Container = styled.div`
  width: 296px;
  padding: 16px 0 0 16px;
`;
const Title = styled.p`
  font-weight: 500;
  font-size: 16px;
  color: #222222;
  line-height: 16px;
  margin: 0;
`;
const Detail = styled.p`
  font-size: 13px;
  color: #b8b9bf;
  line-height: 13px;
  margin: 12px 0px;
`;
const Comp = styled(BigButtonStyle)`
  height: 36px;
  width: 280px;
  margin-top: 8px;
  background: #ffffff;
  border: 1px solid #d7d9e0;
  color: #333333;
  &:last-child {
    margin-bottom: 8px;
  }
  &:hover {
    font-weight: 500;
    background: #fdfdfd;
    border: 1px solid #8b8fa3;
    color: #333333;
  }
  &:focus {
    font-weight: 500;
    background: #fdfdfd;
    border: 1px solid #8b8fa3;
    color: #333333;
  }
`;
const DelBtn = styled(BigButtonStyle)`
  width: auto;
  margin-top: 8px;
  padding: 4px 15px;
  height: 28px;
  color: #f73131;
  background: #fef4f4;
  border: 1px solid #fccdcd;
  &:hover {
    color: #f73131;
    background: #feecec;
    border: 1px solid #fccdcd;
  }
  &:focus {
    color: #f73131;
    background: #feecec;
    border: 1px solid #fccdcd;
  }
`;
const Tiptext = styled.p`
  margin-top: 40px;
  margin-bottom: 70px;

  font-size: 14px;
  color: #333333;
  line-height: 14px;
`;

export type layoutsNodeItem = {
  key: string;
  title: string;
  type?: string;
  children: layoutsNodeItem[];
  z?: number;
};

interface Iprops {
  layoutMode: "grid" | "free",
  treeData: layoutsNodeItem[];
  onSelect: TreeProps['onSelect'];
  delete: () => void;
  onDrop: TreeProps['onDrop'];
  icon?: TreeProps['icon'];
}

export const SelectedComps = (props: Iprops) => {
  const { treeData, onSelect, onDrop, icon } = props;
  const draggable = props.layoutMode === "free"
  return (
    <Container>
      <Title>{trans("comp.selectedCompsTitle", { selectCompNum: Object.keys(treeData).length })}</Title>
      <Detail>{trans("comp.selectedCompsDetail")}</Detail>
      <div>
        <Tree
          showIcon
          blockNode
          draggable={draggable}
          treeData={treeData}
          onDrop={onDrop}
          onSelect={onSelect}
          icon={icon}
        />
      </div>
      <DelBtn onClick={() => props.delete()}>{trans("comp.batchDelete")}</DelBtn>
    </Container>
  );
};
