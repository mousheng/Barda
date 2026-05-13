import { Badge, Tabs } from "antd";
import { JSONObject, JSONValue } from "util/jsonTypes";
import {
  CompAction,
  CompActionTypes,
  deleteCompAction,
  wrapChildAction,
  DispatchType,
  RecordConstructorToView,
  wrapDispatch,
} from "barda-core";
import { AutoHeightControl } from "comps/controls/autoHeightControl";
import { stringExposingStateControl } from "comps/controls/codeStateControl";
import { eventHandlerControl } from "comps/controls/eventHandlerControl";
import { TabsOptionControl } from "comps/controls/optionsControl";
import { styleControl } from "comps/controls/styleControl";
import {
  TabContainerStyle,
  TabContainerStyleType,
  getStandardBoxValuesByDirection,
  parseBoxValues,
  positionType,
} from "comps/controls/styleControlConstants";
import { sameTypeMap, UICompBuilder, withDefault } from "comps/generators";
import { addMapChildAction } from "comps/generators/sameTypeMap";
import { NameConfig, NameConfigHidden, withExposingConfigs } from "comps/generators/withExposing";
import { NameGenerator } from "comps/utils";
import { Section, sectionNames } from "barda-design";
import { HintPlaceHolder } from "barda-design";
import _ from "lodash";
import React from "react";
import styled, { css } from "styled-components";
import { IContainer } from "../containerBase/iContainer";
import { SimpleContainerComp } from "../containerBase/simpleContainerComp";
import { CompTree, mergeCompTrees } from "../containerBase/utils";
import { ContainerBaseProps, gridItemCompToGridItems, InnerGrid } from "../containerComp/containerView";
import { BackgroundColorContext } from "comps/utils/backgroundColorContext";
import { disabledPropertyView, hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import { BoolCodeControl } from "comps/controls/codeControl";
import { DisabledContext } from "comps/generators/uiCompBuilder";
import { useEditorStore } from "comps/editorStore";
import { checkIsMobile } from "util/commonUtils";
import { messageInstance } from "barda-design";
import { PositionControl } from "@barda/comps/controls/dropdownControl";
import { BoolControl } from "@barda/comps/controls/boolControl";
import { Layout } from "@barda/layout";

const EVENT_OPTIONS = [
  {
    label: trans("tabbedContainer.switchTab"),
    value: "change",
    description: trans("tabbedContainer.switchTabDesc"),
  },
] as const;

const childrenMap = {
  tabs: TabsOptionControl,
  selectedTabKey: stringExposingStateControl("key", "Tab1"),
  containers: withDefault(sameTypeMap(SimpleContainerComp), {
    0: { layout: {}, items: {} },
    1: { layout: {}, items: {} },
  }),
  autoHeight: AutoHeightControl,
  onEvent: eventHandlerControl(EVENT_OPTIONS),
  disabled: BoolCodeControl,
  style: styleControl(TabContainerStyle),
  position: withDefault(PositionControl, "top"),
  showHeader: BoolControl.DEFAULT_TRUE,
  labelCentered: BoolControl,
  showScroll: BoolControl.DEFAULT_TRUE,
  cardStyle: BoolControl,
};

type ViewProps = RecordConstructorToView<typeof childrenMap>;
type TabbedContainerProps = ViewProps & { dispatch: DispatchType };

const getStyle = (
  style: TabContainerStyleType,
  position: positionType,
  cardStyle: boolean,
  $showTabs: boolean,
) => {
  let borderWidth = getStandardBoxValuesByDirection(position, cardStyle ? "0px" : "1px", "line");
  let Radius = getStandardBoxValuesByDirection(position, cardStyle ? "0px" : style.radius, "surface");
  let reverseBorderWidth = getStandardBoxValuesByDirection(position, "1px", "line", false);
  let reverseRadius = getStandardBoxValuesByDirection(position, style.radius, "surface", false);
  return css`
    &.ant-tabs {
      border-radius: ${style.radius};
      overflow: hidden;
      > .ant-tabs-content-holder > .ant-tabs-content > div > .react-grid-layout {
        background-color: ${style.background};
        border-radius: 0;
      }
      background-color: ${style.background};
      > .ant-tabs-content-holder > .ant-tabs-content > .ant-tabs-tabpane {
        height: 100%;
        border-style: solid;
        border-color: ${style.border};
        border-width: ${reverseBorderWidth};
        border-radius: ${reverseRadius};
        padding-left: ${position === "left" ? "2px!important" : "0px!important"};
        padding-right: ${position === "right" ? "2px!important" : "0px!important"};
        .react-grid-layout {
          border-radius: ${reverseRadius};
        }
      }

      > .ant-tabs-nav {
        border-style: solid;
        border-color: ${style.border};
        border-width: ${borderWidth};
        border-radius: ${Radius};
        background-color: ${style.headerBackground};
        display: ${(props) => ($showTabs ? "flex" : "none")};

        .ant-tabs-tab {
          background-color: ${style.headerBackground};
          ${position === 'top' ? "margin-bottom: 1px;" : ""};
          ${position === 'bottom' ? "margin-top: 1px;" : ""};
          div {
            color: ${style.tabText};
          }
          > .ant-tabs-nav-wrap > .ant-tabs-nav-list > .ant-tabs-ink-bar {
            background-color: ${style.accent};
          }
        }

        .ant-tabs-ink-bar {
          background-color: ${style.accent};
        }
        .ant-tabs-tab-active {
          border-radius: 4px;
          background-color: ${style.activeTabBackground};
        }

        ::before {
          border-color: ${style.border};
        }
      }
    }
  `;
};

const StyledTabs = styled(Tabs)<{
  $style: TabContainerStyleType;
  $position: positionType;
  $cardStyle: boolean;
  $isMobile?: boolean;
  $autoHeight: boolean;
  $showTabs: boolean;
}>`
  &.ant-tabs {
    height: 100%;
  }
  .ant-tabs-nav-operations {
    display: ${(props) =>
      (props.tabPosition === "left" || props.tabPosition === "right") && props.$autoHeight
        ? "none!important"
        : ""};
  }
  .ant-tabs-nav-wrap {
    border-right: ${(props) => (props.tabPosition === "left" ? "1px solid " + props.$style.border : "")};
    border-left: ${(props) => (props.tabPosition === "right" ? "1px solid" + props.$style.border : "")};
  }
  .ant-tabs-content-animated {
    transition-duration: 0ms;
  }

  .ant-tabs-content {
    height: 100%;
    // margin-top: -16px;
  }

  .ant-tabs-nav {
    padding: 0
      ${(props) =>
        props.tabPosition === "top" || props.tabPosition === "bottom" ? (props.$isMobile ? 16 : 24) : 0}px;
    background: white;
    margin: 0px;
    &::before {
      border-color: ${(props) => props.$style.border};
    }
  }

  .ant-tabs-tab + .ant-tabs-tab {
    margin: 0 0 0 20px;
  }

  ${(props) =>
    props.$style &&
    getStyle(props.$style, props.$position, props.$cardStyle, props.$showTabs)}
`;

const TabItemBadge = styled(Badge)<{ $color: string }>`
  color: ${(props) => props.$color};
`;

const ContainerInTab = (props: ContainerBaseProps & { showScroll: boolean }) => {
  return <InnerGrid {...props} emptyRows={15} bgColor={"white"} hintPlaceholder={HintPlaceHolder} />;
};

const TabbedContainer = (props: TabbedContainerProps) => {
  let { tabs, containers, dispatch, style } = props;

  const visibleTabs = tabs.filter((tab) => !tab.hidden);
  const selectedTab = visibleTabs.find((tab) => tab.key === props.selectedTabKey.value);
  const activeKey = selectedTab ? selectedTab.key : visibleTabs.length > 0 ? visibleTabs[0].key : undefined;

  // const onTabClick = useCallback(
  //   (key: string, event: React.KeyboardEvent<Element> | React.MouseEvent<Element, MouseEvent>) => {
  //     // log.debug("onTabClick. event: ", event);
  //     const target = event.target;
  //     (target as any).parentNode.click
  //       ? (target as any).parentNode.click()
  //       : (target as any).parentNode.parentNode.click();
  //   },
  //   []
  // );

  const maxWidth = useEditorStore((s) => s.rootComp?.children.settings.getView().maxWidth);
  const isMobile = checkIsMobile(maxWidth);
  const bodyPadding = parseBoxValues(props.style.padding_UNIT, [11, 19, 11, 19], false) as number[];
  const containerPadding: [number, number] = [bodyPadding[1], bodyPadding[0]];

  // log.debug("TabbedContainer. props: ", props);

  const tabItems = visibleTabs.map((tab) => {
    // log.debug("Tab. tab: ", tab, " containers: ", containers);
    const id = String(tab.id);
    const childDispatch = wrapDispatch(wrapDispatch(dispatch, "containers"), id);
    const containerProps = containers[id].children;
    const hasIcon = tab.icon.props.value;
    const label = (
      <TabItemBadge
        count={tab.count}
        size="small"
        offset={[4, -6]}
        $color={tab.disabled ? "#cdcdcd" : props.style.tabText}
      >
        {tab.iconPosition === "left" && hasIcon && <span style={{ marginRight: "4px" }}>{tab.icon}</span>}
        {tab.label}
        {tab.iconPosition === "right" && hasIcon && <span style={{ marginLeft: "4px" }}>{tab.icon}</span>}
      </TabItemBadge>
    );
    return {
      label,
      key: tab.key,
      forceRender: true,
      disabled: tab.disabled,
      children: (
        <BackgroundColorContext.Provider value={props.style.background}>
          <ContainerInTab
            showScroll={props.showScroll}
            layout={containerProps.layout.getView()}
            items={gridItemCompToGridItems(containerProps.items.getView())}
            positionParams={containerProps.positionParams.getView()}
            dispatch={childDispatch}
            autoHeight={props.autoHeight}
            containerPadding={containerPadding}
          />
        </BackgroundColorContext.Provider>
      ),
    };
  });
  return (
    <StyledTabs
      activeKey={activeKey}
      $style={style}
      $position={props.position ?? "top"}
      $cardStyle={props.cardStyle ?? false}
      onChange={(key: string) => {
        if (key !== props.selectedTabKey.value) {
          props.selectedTabKey.onChange(key);
          props.onEvent("change");
        }
      }}
      $showTabs={props.showHeader}
      centered={props.labelCentered}
      animated
      $isMobile={isMobile}
      $autoHeight={props.autoHeight}
      type={props.cardStyle ? "card" : "line"}
      // tabBarGutter={32}
      items={tabItems}
      tabPosition={props.position ?? "top"}
    ></StyledTabs>
  );
};

export const TabbedContainerBaseComp = (function () {
  return new UICompBuilder(childrenMap, (props, dispatch) => {
    return (
      <DisabledContext.Provider value={props.disabled}>
        <TabbedContainer {...props} dispatch={dispatch} />
      </DisabledContext.Provider>
    );
  })
    .setPropertyViewFn((children) => {
      return (
        <>
          <Section name={sectionNames.basic}>
            {children.tabs.propertyView({
              title: trans("tabbedContainer.tab"),
              newOptionLabel: "Tab",
            })}
            {children.showHeader.propertyView({ label: trans("tabbedContainer.showTabs") })}
            {children.showHeader.getView() &&
              children.position.propertyView({
                label: trans("tabbedContainer.TabPosition"),
                radioButton: true,
              })}
            {children.showHeader.getView() &&
              (children.position.getView() === "top" || children.position.getView() === "bottom") &&
              children.labelCentered.propertyView({ label: trans("tabbedContainer.labelCentered") })}
            {children.showHeader.getView() &&
              children.cardStyle.propertyView({ label: trans("tabbedContainer.cardStyle") })}
            {children.selectedTabKey.propertyView({ label: trans("prop.defaultValue") })}
            {children.autoHeight.getPropertyView()}
            {!children.autoHeight.getView() && children.showScroll.propertyView({ label: trans("container.showScroll") })}
          </Section>
          <Section name={sectionNames.interaction}>
            {children.onEvent.getPropertyView()}
            {disabledPropertyView(children)}
          </Section>
          <Section name={sectionNames.layout}>
            {hiddenPropertyView(children)}
            {children.showScroll.propertyView({ label: trans("container.showScroll") })}
          </Section>
          <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
        </>
      );
    })
    .build();
})();

class TabbedContainerImplComp extends TabbedContainerBaseComp implements IContainer {
  private syncContainers(): this {
    const tabs = this.children.tabs.getView();
    const ids: Set<string> = new Set(tabs.map((tab) => String(tab.id)));
    let containers = this.children.containers.getView();
    // delete
    const actions: CompAction[] = [];
    Object.keys(containers).forEach((id) => {
      if (!ids.has(id)) {
        // log.debug("syncContainers delete. ids=", ids, " id=", id);
        actions.push(wrapChildAction("containers", wrapChildAction(id, deleteCompAction())));
      }
    });
    // new
    ids.forEach((id) => {
      if (!containers.hasOwnProperty(id)) {
        // log.debug("syncContainers new containers: ", containers, " id: ", id);
        actions.push(wrapChildAction("containers", addMapChildAction(id, { layout: {}, items: {} })));
      }
    });

    // log.debug("syncContainers. actions: ", actions);
    let instance = this;
    actions.forEach((action) => {
      instance = instance.reduce(action);
    });
    return instance;
  }

  override reduce(action: CompAction): this {
    if (action.type === CompActionTypes.CUSTOM) {
      const value = action.value as JSONObject;
      if (value.type === "push") {
        const itemValue = value.value as JSONObject;
        if (_.isEmpty(itemValue.key)) itemValue.key = itemValue.label;
        action = {
          ...action,
          value: {
            ...value,
            value: { ...itemValue },
          },
        } as CompAction;
      }
      if (value.type === "delete" && this.children.tabs.getView().length <= 1) {
        messageInstance.warning(trans("tabbedContainer.atLeastOneTabError"));
        // at least one tab
        return this;
      }
    }
    // log.debug("before super reduce. action: ", action);
    let newInstance = super.reduce(action);
    if (action.type === CompActionTypes.UPDATE_NODES_V2) {
      // Need eval to get the value in StringControl
      newInstance = newInstance.syncContainers();
    }
    // log.debug("reduce. instance: ", this, " newInstance: ", newInstance);
    return newInstance;
  }

  realSimpleContainer(key?: string): SimpleContainerComp | undefined {
    let selectedTabKey = this.children.selectedTabKey.getView().value;
    const tabs = this.children.tabs.getView();
    const selectedTab = tabs.find((tab) => tab.key === selectedTabKey) ?? tabs[0];
    const id = String(selectedTab.id);
    if (_.isNil(key)) return this.children.containers.children[id];
    return Object.values(this.children.containers.children).find((container) =>
      container.realSimpleContainer(key)
    );
  }

  getCompTree(): CompTree {
    const containerMap = this.children.containers.getView();
    const compTrees = Object.values(containerMap).map((container) => container.getCompTree());
    return mergeCompTrees(compTrees);
  }

  getAllLayouts(): Layout {
    return _.reduce(
      this.children.containers.getView(),
      (acc, container) => {
        return _.merge(acc, container.getAllLayouts());
      },
      {}
    );
  }

  findContainer(key: string): IContainer | undefined {
    const containerMap = this.children.containers.getView();
    for (const container of Object.values(containerMap)) {
      const foundContainer = container.findContainer(key);
      if (foundContainer) {
        return foundContainer === container ? this : foundContainer;
      }
    }
    return undefined;
  }

  getPasteValue(nameGenerator: NameGenerator): JSONValue {
    const containerMap = this.children.containers.getView();
    const containerPasteValueMap = _.mapValues(containerMap, (container) =>
      container.getPasteValue(nameGenerator)
    );

    return { ...this.toJsonValue(), containers: containerPasteValueMap };
  }

  override autoHeight(): boolean {
    return this.children.autoHeight.getView();
  }
}

export const TabbedContainerComp = withExposingConfigs(TabbedContainerImplComp, [
  new NameConfig("selectedTabKey", trans("tabbedContainer.selectedTabKeyDesc")),
  NameConfigHidden,
]);
