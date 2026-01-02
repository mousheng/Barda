import { CloseOutlined } from "@ant-design/icons";
import { Button } from "antd";
import { changeChildAction } from "barda-core";
import { AlignClose, AlignLeft, AlignRight, Drawer, HintPlaceHolder, Section, sectionNames } from "barda-design";
import { ContainerCompBuilder } from "comps/comps/containerBase/containerCompBuilder";
import { gridItemCompToGridItems, InnerGrid } from "comps/comps/containerComp/containerView";
import { AutoHeightControl } from "comps/controls/autoHeightControl";
import { BoolControl } from "comps/controls/boolControl";
import { NumberControl, StringControl } from "comps/controls/codeControl";
import { booleanExposingStateControl } from "comps/controls/codeStateControl";
import { dropdownControl, PositionControl } from "comps/controls/dropdownControl";
import { closeEvent, eventHandlerControl } from "comps/controls/eventHandlerControl";
import { styleControl } from "comps/controls/styleControl";
import { DrawerStyle, parseBoxValues } from "comps/controls/styleControlConstants";
import { withDefault } from "comps/generators";
import { withMethodExposing } from "comps/generators/withMethodExposing";
import { BackgroundColorContext } from "comps/utils/backgroundColorContext";
import { CanvasContainerID } from "constants/domLocators";
import { Layers } from "constants/Layers";
import { trans } from "i18n";
import { useCallback, useState } from "react";
import styled from "styled-components";
import { useUserViewMode } from "util/hooks";
import { NameConfig, withExposingConfigs } from "../generators/withExposing";

const EventOptions = [closeEvent] as const;

const DEFAULT_WIDTH = 378;

const ButtonStyle = styled(Button)<{ placement: string }>`
  position: absolute;
  border: none !important;
  ${(props) => (props.placement === "start" ? "left: 0" : "right: 0")};
  top: 0;
  z-index: 10;
  font-weight: 700;
  box-shadow: none;
  color: rgba(0, 0, 0, 0.45);
  height: 54px;
  width: 54px;

  svg {
    width: 16px;
    height: 16px;
  }

  &,
  :hover,
  :focus {
    background-color: transparent;
    border: none;
  }

  :hover,
  :focus {
    color: rgba(0, 0, 0, 0.75);
  }
`;

const PositionType = [
  {
    label: <AlignLeft />,
    value: "start",
  },
  {
    label: <AlignRight />,
    value: "end",
  },
  {
    label: <AlignClose />,
    value: "none",
  },
] as const;

let TmpDrawerComp = (function () {
  return new ContainerCompBuilder(
    {
      visible: booleanExposingStateControl("visible"),
      onEvent: eventHandlerControl(EventOptions),
      size: withDefault(NumberControl, DEFAULT_WIDTH),
      title: withDefault(StringControl, ""),
      closeButtonPlacement: dropdownControl(PositionType, "start"),
      autoHeight: AutoHeightControl,
      style: styleControl(DrawerStyle),
      placement: PositionControl,
      maskClosable: withDefault(BoolControl, true),
      showMask: withDefault(BoolControl, true),
      showScroll: BoolControl,
    },
    (props, dispatch) => {
      const isTopBom = ["top", "bottom"].includes(props.placement);
      const { items, ...otherContainerProps } = props.container;
      const userViewMode = useUserViewMode();
      const resizable = !userViewMode && (!isTopBom || !props.autoHeight);
      const closeBtnPos = props.closeButtonPlacement || "start";
      const containerPadding = parseBoxValues(props.style.bodyPadding_UNIT, [3, 19, 3, 19], false) as number[];
      const onResizeStop = useCallback(
        (size: number) => {
          dispatch(changeChildAction("size", Number(size), true));
        },
        [dispatch]
      );
      return (
        <BackgroundColorContext.Provider value={props.style.background}>
          <Drawer
            autoHeight={props.autoHeight}
            title={props.title || undefined}
            closable={props.title === "" || closeBtnPos === "none" ? false : { placement: closeBtnPos }}
            resizable={resizable}
            onResizeStop={onResizeStop}
            style={props.visible.value ? { overflow: "auto", pointerEvents: "auto" } : {}}
            styles={{
              body: { padding: 0, backgroundColor: props.style.background },
              wrapper: { maxHeight: "100%", maxWidth: "100%" },
            }}
            placement={props.placement}
            open={props.visible.value}
            size={props.size}
            getContainer={() => document.querySelector(`#${CanvasContainerID}`) || document.body}
            onClose={() => {
              props.visible.onChange(false);
            }}
            afterOpenChange={(visible) => {
              if (!visible) {
                props.onEvent("close");
              }
            }}
            zIndex={Layers.drawer}
            maskClosable={props.maskClosable}
            mask={props.showMask}
          >
            {props.title === "" && props.closeButtonPlacement !== "none" && (
              <ButtonStyle
                onClick={() => {
                  props.visible.onChange(false);
                }}
                placement={closeBtnPos}
              >
                <CloseOutlined />
              </ButtonStyle>
            )}
            <InnerGrid
              {...otherContainerProps}
              items={gridItemCompToGridItems(items)}
              autoHeight={props.autoHeight && isTopBom}
              minHeight={isTopBom  ? (props.autoHeight ? DEFAULT_WIDTH+'px' : props.size + "px") : "100%" }
              showScroll={props.showScroll}
              containerPadding={[containerPadding[1], containerPadding[0]]}
              hintPlaceholder={HintPlaceHolder}
              bgColor={props.style.background}
            />
          </Drawer>
        </BackgroundColorContext.Provider>
      );
    }
  )
    .setPropertyViewFn((children) => (
      <>
        <Section name={sectionNames.basic}>
          {children.title.propertyView({ label: trans("drawer.title") })}
          {children.closeButtonPlacement.propertyView({
            label: trans("drawer.closeButtonPlacement"),
            radioButton: true,
          })}
          {children.placement.propertyView({ label: trans("drawer.placement"), radioButton: true })}
          {["top", "bottom"].includes(children.placement.getView()) && children.autoHeight.getPropertyView()}
          {["top", "bottom"].includes(children.placement.getView()) &&
            !children.autoHeight.getView() &&
            children.size.propertyView({
              label: trans("drawer.height"),
              tooltip: trans("drawer.heightTooltip"),
              placeholder: DEFAULT_WIDTH + "",
            })}
          {(children.autoHeight.getView() === false || ["left", "right"].includes(children.placement.getView())) &&
            children.showScroll.propertyView({
              label: trans("container.showScroll"),
            })}
          {children.showMask.propertyView({
            label: trans("prop.showMask"),
          })}
          {children.showMask.getView() &&
            children.maskClosable.propertyView({
              label: trans("prop.maskClosable"),
            })}
        </Section>
        <Section name={sectionNames.interaction}>{children.onEvent.getPropertyView()}</Section>
        <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
      </>
    ))
    .build();
})();

TmpDrawerComp = class extends TmpDrawerComp {
  override autoHeight(): boolean {
    return false;
  }
};

TmpDrawerComp = withMethodExposing(TmpDrawerComp, [
  {
    method: {
      name: "openDrawer",
      description: trans("drawer.openDrawerDesc"),
      params: [],
    },
    execute: (comp, values) => {
      comp.children.visible.getView().onChange(true);
    },
  },
  {
    method: {
      name: "closeDrawer",
      description: trans("drawer.closeDrawerDesc"),
      params: [],
    },
    execute: (comp, values) => {
      comp.children.visible.getView().onChange(false);
    },
  },
]);

export const DrawerComp = withExposingConfigs(TmpDrawerComp, [new NameConfig("visible", trans("export.visibleDesc"))]);
