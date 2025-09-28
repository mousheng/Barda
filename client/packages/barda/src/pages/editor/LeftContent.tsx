import { DoubleLeftOutlined } from "@ant-design/icons";
import {
  BaseSection,
  Collapse,
  CopyTextButton,
  CollapseLabel as Label,
  PadDiv,
  ScrollBar,
  CollapseTitle as Title,
  Tooltip,
  UnShow
} from "barda-design";
import { Tabs, TabTitle } from "components/Tabs";
import UIComp from "comps/comps/uiComp";
import { EditorContext, SelectSourceType } from "comps/editorState";
import { BackgroundColor, TopHeaderHeight } from "constants/style";
import { trans } from "i18n";
import _ from "lodash";
import React, { memo, ReactNode, useCallback, useContext, useMemo, useState } from "react";
import { useDeepCompareEffect } from "react-use";
import styled from "styled-components";
import { BottomResTypeEnum } from "types/bottomRes";
import { getComponentTree, safeJSONStringify } from "util/objectUtils";
import { TogglePanel } from "../common/header";
import { CloseButton } from "./codeEditorPanel";
import { BottomResSection } from "./left/bottomResSection";
import { GlobalVariablesSection } from "./left/globalVariablesSection";
import { UICompsListSection } from "./left/leftUICompsListSection";

const CollapseTitleWrapper = styled.div`
  display: flex;
  width: fit-content;
  max-width: calc(100% - 8px);
`;

function getLen(config: string | boolean | number) {
  if (typeof config === "number") {
    return (config + "").toString().length;
  }
  if (typeof config === "string" || typeof config === "boolean") {
    return config.toString().length;
  }
  return 0;
}

const ToDataView = memo((props: { value: any, name: string, desc?: ReactNode }) => {
  const { value, name, desc } = props;
  const str = typeof value === "function" ? "Function" : safeJSONStringify(value);
  const descRecord: Record<string, ReactNode> = {};
  descRecord[name] = desc;
  if (Array.isArray(value)) {
    const dataChild: Record<string, any> = {};
    value.forEach((valueChild, index) => {
      dataChild[index] = valueChild;
    });
    return (
      <CollapseView name={name} desc={descRecord} data={dataChild} isArray={true} key={name} />
    );
  } else if (_.isPlainObject(value)) {
    return <CollapseView name={name} desc={descRecord} data={value} key={name} />;
  }
  return (
    <PadDiv key={name}>
      <Tooltip title={desc} placement={"right"}>
        <Label label={name} />
        &#8203;
      </Tooltip>

      <Tooltip
        title={
          getLen(str) > 50 ? (
            <div style={{ display: "flex", wordBreak: "break-all" }}>
              {getLen(str) > 300 ? str.slice(0, 300) + "..." : str}
              <CopyTextButton text={value} style={{ color: "#fff", margin: "4px 0 0 6px" }} />
            </div>
          ) : null
        }
        placement={"right"}
      >
        &#8203;
        <Label color="#FF9816" label={getLen(str) > 50 ? str.slice(0, 50) + "..." : str} />
      </Tooltip>
    </PadDiv>
  );
})

function sliceArr(arr: string[]) {
  let preArr: string[] = [];
  let afterArr: string[] = [];
  arr.forEach((arrChild, index) => {
    if (index < 15) {
      preArr.push(arrChild);
    }
    if (index >= arr.length - 5 && index < arr.length) {
      afterArr.push(arrChild);
    }
  });
  return { preArr, afterArr } as const;
}

function toData(props: { data: Record<string, any>; desc?: Record<string, ReactNode> }) {
  const totalArr = Object.keys(props.data);
  return (
    <div>
      {totalArr.length < 30 ? (
        totalArr.map((name) => <ToDataView key={name} value={props.data[name]} name={name} desc={props.desc?.[name]} />)
      ) : (
        <>
          {sliceArr(totalArr).preArr.map((name) => <ToDataView key={name} value={props.data[name]} name={name} desc={props.desc?.[name]} />)}
          <UnShow num={totalArr.length - 6} />
          {sliceArr(totalArr).afterArr.map((name) => <ToDataView key={name} value={props.data[name]} name={name} desc={props.desc?.[name]} />)}
        </>
      )}
    </div>
  );
}

export const CollapseView = React.memo(
  (props: {
    name: string;
    desc?: Record<string, ReactNode>;
    data: Record<string, any>;
    isArray?: boolean;
    onClick?: (compName: string, resType?: BottomResTypeEnum) => void;
    isSelected?: boolean;
    isOpen?: boolean;
    resType?: BottomResTypeEnum,
  }) => {
    const { data = {} } = props;
    const onlyOne = Object.keys(data).length === 1;
    const handleClick = useCallback(() => props.onClick && props.onClick(props.name, props?.resType), [props.onClick, props.name, props?.resType]);
    const items = useMemo(() => [
      {
        key: props.name,
        title: (
          <Tooltip
            title={props.desc?.[props.name]}
            placement={"right"}
          >
            <CollapseTitleWrapper onClick={handleClick}>
              <Title
                style={{
                  whiteSpace: "nowrap",
                  textOverflow: "ellipsis",
                  overflow: "hidden",
                }}
                label={props.name}
                hasChild={Object.keys(data).length > 0}
              />
              <Title
                style={{ flexShrink: 0 }}
                color="#8B8FA3"
                label={`${props.isArray ? "[]" : "{}"} ${trans(
                  props.isArray
                    ? onlyOne
                      ? "leftPanel.propTipArr"
                      : "leftPanel.propTipsArr"
                    : onlyOne
                      ? "leftPanel.propTip"
                      : "leftPanel.propTips",
                  {
                    num: Object.keys(data).length,
                  }
                )}`}
              />
            </CollapseTitleWrapper>
          </Tooltip>
        ),
        data: toData({ data, desc: props.desc }),
      },
    ], [data, handleClick, onlyOne, props.desc, props.isArray, props.name])
    return (
      <Collapse
        isSelected={props.isSelected}
        isOpen={props.isOpen}
        config={items}
      />
    );
  }
);

interface LeftContentProps {
  uiComp: InstanceType<typeof UIComp>;
  togglePanel: TogglePanel
}

enum LeftTabKey {
  State = "state",
  ModuleSetting = "module-setting",
}

const CloseButton2 = styled(CloseButton)`
  position: absolute;
  top: 8px;
  right: 8px;
`

const LeftContentTabs = styled(Tabs)`
  .ant-tabs-nav {
    background-color: ${BackgroundColor};
    height: 40px;
    padding: 0 16px;
    margin: 0;

    .ant-tabs-tab {
      padding: 0;
      font-weight: 500;
    }
  }

  .ant-tabs-tabpane {
    height: calc(100vh - ${TopHeaderHeight} - 40px);
  }
`;
const LeftContentWrapper = styled.div`
  height: calc(100vh - ${TopHeaderHeight});
`;

export const LeftContent = React.memo((props: LeftContentProps) => {
  const { uiComp } = props;
  const editorState = useContext(EditorContext);
  const globalVariablesInfoList = useMemo(() => editorState.hooksCompInfoList(),
    [editorState.rootComp.children.hooks])

  const UITreeData = useMemo(() => {
    return getComponentTree(editorState.getUIComp().getTree(), [])
  }, [editorState.rootComp.children.ui])

  // 对话框面板数据
  const [selectedCompId, setSelectedCompId] = useState<string[]>([]);
  const ModalTreeData = useMemo(() => {
    return getComponentTree(editorState.getHooksComp().getUITree(), [])
  }, [editorState.rootComp.children.hooks])

  useDeepCompareEffect(() => {
    if (editorState.selectedCompNames.size > 0) {
      setSelectedCompId([Object.keys(editorState.selectedComps())[0]])
    }
  }, [Array.from(editorState.selectedCompNames).sort()])

  const clickNode = useCallback((selectedCompNames: Set<string>, selectSource?: SelectSourceType) =>
    editorState.setSelectedCompNames(selectedCompNames, selectSource),
    [editorState.setSelectedCompNames])

  const handleBottomResItemClick = useCallback(
    (name: string, type?: BottomResTypeEnum) => {
      editorState.setSelectedBottomRes(name, type);
    },
    [editorState.setSelectedBottomRes]
  );

  const bottomResList = useMemo(() => editorState.bottomResComInfoList(), [
    editorState.rootComp.children.hooks,
    editorState.rootComp.children.queries,
    editorState.rootComp.children.tempStates,
    editorState.rootComp.children.dataResponders,
    editorState.rootComp.children.transformers,
  ]);

  const selectedBottomResName = useMemo(() => editorState.selectedBottomResName, [editorState.selectedBottomResName]);

  const moduleLayoutComp = uiComp.getModuleLayoutComp();
  const stateContent = (
    <ScrollBar>
      <div style={{ paddingBottom: 80 }}>
        <CloseButton2
          onClick={() => props.togglePanel("left")}>
          <DoubleLeftOutlined />
        </CloseButton2>
        <UICompsListSection
          treeData={UITreeData}
          selectedKeys={selectedCompId}
          clickNode={clickNode}
          sectionName={trans("leftPanel.components")}
          height={500}
        />
        <UICompsListSection
          treeData={ModalTreeData}
          selectedKeys={selectedCompId}
          clickNode={clickNode}
          sectionName={trans("leftPanel.modals")}
          height={300}
        />
        <BottomResSection
          bottomResList={bottomResList}
          selectedBottomResName={selectedBottomResName}
          onBottomResClick={handleBottomResItemClick}
        />
        <GlobalVariablesSection globalVariablesInfoList={globalVariablesInfoList} />
      </div>
    </ScrollBar>
  );

  if (!moduleLayoutComp) {
    return <LeftContentWrapper className="cypress-left-content">{stateContent}</LeftContentWrapper>;
  }

  return (
    <LeftContentWrapper className="cypress-left-content">
      <LeftContentTabs defaultActiveKey={LeftTabKey.ModuleSetting}>
        <Tabs.TabPane key={LeftTabKey.State} tab={<TabTitle text={trans("leftPanel.stateTab")} />}>
          {stateContent}
        </Tabs.TabPane>
        <Tabs.TabPane
          key={LeftTabKey.ModuleSetting}
          tab={<TabTitle text={trans("leftPanel.settingsTab")} />}
        >
          <ScrollBar>
            <div style={{ paddingBottom: 80, paddingTop: 16 }}>
              <BaseSection width={288} noMargin>
                <span>{moduleLayoutComp.getConfigView()}</span>
              </BaseSection>
            </div>
          </ScrollBar>
        </Tabs.TabPane>
      </LeftContentTabs>
    </LeftContentWrapper>
  );
}, (prevProps, nextProps) => {
  return _.isEqual(prevProps, nextProps);
},);
