import { BoolCodeControl, StringControl } from "comps/controls/codeControl";
import { trans } from "i18n";
import { Switch } from "antd";
import { ColumnTypeCompBuilder, ColumnTypeViewFn } from "../columnTypeCompBuilder";
import { ColumnValueTooltip } from "../simpleColumnTypeComps";
import styled, { css } from "styled-components";
import { SwitchStyle, SwitchStyleType } from "comps/controls/styleControlConstants";
import { useStyle } from "comps/controls/styleControl";
import React from "react";
import { changeChildAction } from "barda-core";
import { eventHandlerControl } from "comps/controls/eventHandlerControl";
import { changeEvent } from "comps/controls/eventHandlerControl";
import { BoolControl } from "@barda/comps/controls/boolControl";

const getStyle = ($style: SwitchStyleType) => {
  return css`
    .ant-switch-handle::before {
      background-color: ${$style.handle};
    }
    button {
      background-image: none;
      background-color: ${$style.unchecked};
      &.ant-switch-checked {
        background-color: ${$style.checked};
      }
    }
  `;
};

const SwitchStyled = styled(Switch) <{ $style: SwitchStyleType }>`
  ${(props) => props.$style && getStyle(props.$style)}
`;

const EventOptions = [
  changeEvent,
  {
    label: trans("switchComp.open"),
    value: "true",
    description: trans("switchComp.openDesc"),
  },
  {
    label: trans("switchComp.close"),
    value: "false",
    description: trans("switchComp.closeDesc"),
  },
] as const;

const childrenMap = {
  text: BoolCodeControl,
  checkedChildren: StringControl,
  unCheckedChildren: StringControl,
  onEvent: eventHandlerControl(EventOptions),
  editable: BoolControl,
};

const getBaseValue: ColumnTypeViewFn<typeof childrenMap, boolean, boolean> = (props) => props.text;

export const SwitchComp = (function () {
  return new ColumnTypeCompBuilder(
    childrenMap,
    (props, dispatch) => {
      const value = props.changeValue ?? getBaseValue(props, dispatch);
      const checkedChildren = props.checkedChildren;
      const unCheckedChildren = props.unCheckedChildren;
      const SwitchDisplayComp = () => {
        const style = useStyle(SwitchStyle);
        // 根据 editable 属性决定是否禁用
        const disabled = !props.editable;
        return (
          <SwitchStyled
            $style={style}
            checked={value}
            checkedChildren={checkedChildren}
            unCheckedChildren={unCheckedChildren}
            disabled={disabled}
            onChange={(checked) => {
              dispatch(changeChildAction("changeValue", checked, false));
              props.onEvent("change");
              props.onEvent(checked ? "true" : "false");
            }}
          />
        );
      };
      return <SwitchDisplayComp />;
    },
    (nodeValue) => nodeValue.text.value,
    getBaseValue
  )
    .setPropertyViewFn((children) => {
      return (
        <>
          {children.text.propertyView({
            label: trans("table.columnValue"),
            tooltip: ColumnValueTooltip,
          })}
          {children.checkedChildren.propertyView({
            label: trans("table.checkedChildren"),
          })}
          {children.unCheckedChildren.propertyView({
            label: trans("table.unCheckedChildren"),
          })}
          {children.editable.propertyView({
            label: trans("table.editable", ),
          })}
          {children.editable.getView() && children.onEvent.propertyView()}
        </>
      );
    })
    .build();
})();

