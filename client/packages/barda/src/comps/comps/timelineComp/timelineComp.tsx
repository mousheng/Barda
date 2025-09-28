import { changeChildAction, CompAction, RecordConstructorToView } from "barda-core";
import { Section, sectionNames } from "barda-design";
import { hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import { useEffect } from "react";
import { UICompBuilder, withDefault } from "../../generators";
import { BoolControl } from "comps/controls/boolControl";
import { jsonControl, StringControl } from "comps/controls/codeControl";
import { dropdownControl } from "comps/controls/dropdownControl";
import { clickEvent, eventHandlerControl, } from "comps/controls/eventHandlerControl";
import { styleControl } from "comps/controls/styleControl";
import { calculateRemainingHeight, calculateRemainingWidth, formatBoxValuesWithUnit, parseBoxValues, TimeLineStyle, TimeLineStyleType, } from "comps/controls/styleControlConstants";
import { valueComp } from "comps/generators/simpleGenerators";
import { NameConfig, NameConfigHidden, withExposingConfigs, } from "comps/generators/withExposing";
import { Timeline } from "antd";
import { DynamicAntdIcon, preloadCommonIcons } from "./antIcon";
import { TimelineDataTooltip, timelineDate, timelineNode } from "./timelineConstants";
import { convertTimeLineData } from "./timelineUtils";
import styled from "styled-components";
import { AutoHeightControl } from "@barda/comps/controls/autoHeightControl";

const Wrapper = styled.div<{
  $style: TimeLineStyleType
}>`
  ${props => `margin: ${formatBoxValuesWithUnit(props.$style.margin_UNIT, [3, 3, 3, 3])}`};
  ${props => `padding: ${formatBoxValuesWithUnit(props.$style.padding_UNIT, [20, 10, 0, 10])}`};
  ${props => `width: ${calculateRemainingWidth(props.$style.margin_UNIT, [3, 3, 3, 3])}`};
  ${props => `height: ${calculateRemainingHeight(props.$style.margin_UNIT, [3, 3, 3, 3])}`};
  ${props => `background: ${props.$style.background}`};
  ${props => `border-radius: ${props.$style.radius}`};
  overflow: auto;
  overflow-x: hidden;

  .ant-timeline .ant-timeline-item-head {
    background-color: transparent;
  }
`;

const EventOptions = [
  clickEvent,
] as const;

const modeOptions = [
  { label: trans("timeLine.left"), value: "left" },
  { label: trans("timeLine.right"), value: "right" },
  { label: trans("timeLine.alternate"), value: "alternate" },
] as const;

const childrenMap = {
  value: jsonControl(convertTimeLineData, timelineDate),
  mode: dropdownControl(modeOptions, "alternate"),
  reverse: BoolControl,
  pending: withDefault(StringControl, trans("timeLine.defaultPending")),
  autoHeight: withDefault(AutoHeightControl, "fixed"),
  onEvent: eventHandlerControl(EventOptions),
  style: styleControl(TimeLineStyle),
  clickedObject: valueComp<timelineNode>({ title: "" }),
  clickedIndex: valueComp<number>(0),
};

const TimelineComp = (
  props: RecordConstructorToView<typeof childrenMap> & {
    dispatch: (action: CompAction) => void;
  }
) => {
  const { value, dispatch, style, onEvent } = props;

  // 预加载常用图标以提升性能（可选）
  useEffect(() => {
    preloadCommonIcons([
      'HomeOutlined',
      'SettingOutlined',
      'UserOutlined',
      'SearchOutlined',
      'PlusOutlined',
      'EditOutlined',
      'DeleteOutlined',
      'CheckOutlined',
      'CloseOutlined',
      'LoadingOutlined',
      'InfoCircleOutlined',
      'ExclamationCircleOutlined',
      'WarningOutlined',
      'StarOutlined',
      'HeartOutlined',
      'EyeOutlined',
      'LockOutlined',
      'MailOutlined',
      'PhoneOutlined',
      'CalendarOutlined'
    ]);
  }, []);
  const timelineItems = value.map((item: timelineNode, index: number) => ({
    key: index,
    color: item?.color,
    dot: item?.dot ? (
      <DynamicAntdIcon
        iconName={item.dot}
        style={{ fontSize: '16px' }}
      />
    ) : undefined,
    label: (
      <span style={{ color: item?.lableColor || style?.lableColor }}>
        {item?.label}
      </span>
    ),
    children: (
      <>
        <button
          onClick={() => {
            dispatch(changeChildAction("clickedObject", item, false));
            dispatch(changeChildAction("clickedIndex", index, false));
            onEvent("click");
          }}
          style={{
            cursor: "pointer",
            color: item?.titleColor || style?.titleColor,
            background: "none",
            border: "none",
            padding: 0,
            font: "inherit",
            textAlign: "left",
          }}
        >
          <b>{item?.title}</b>
        </button>
        <p style={{ color: item?.subTitleColor || style?.subTitleColor }}>
          {item?.subTitle}
        </p>
      </>
    ),
  }));

  return (
    <Wrapper $style={style}>
      <Timeline
        mode={props?.mode || "left"}
        reverse={props?.reverse}
        pending={
          props?.pending && (
            <span style={{ color: style?.titleColor }}>
              {props?.pending || ""}
            </span>
          )
        }
        items={timelineItems}
      />
    </Wrapper>
  );
};

let TimeLineBasicComp = (function () {
  return new UICompBuilder(childrenMap, (props, dispatch) => (
    <TimelineComp {...props} dispatch={dispatch} />
  ))
    .setPropertyViewFn((children) => (
      <>
        <Section name={sectionNames.basic}>
          {children.value.propertyView({
            label: trans("timeLine.value"),
            tooltip: TimelineDataTooltip,
            placeholder: "[]",
          })}
          {children.mode.propertyView({
            label: trans("timeLine.mode"),
            tooltip: trans("timeLine.modeTooltip"),
          })}
          {children.reverse.propertyView({
            label: trans("timeLine.reverse"),
          })}
          {children.pending.propertyView({
            label: trans("timeLine.pending"),
          })}
          {children.autoHeight.propertyView({
            label: trans("timeLine.autoHeight"),
          })}
        </Section>
        <Section name={sectionNames.layout}>
          {children.onEvent.getPropertyView()}
          {hiddenPropertyView(children)}
        </Section>
        <Section name={sectionNames.style}>
          {children.style.getPropertyView()}
        </Section>
      </>
    ))
    .build();
})();

TimeLineBasicComp = class extends TimeLineBasicComp {
  override autoHeight(): boolean {
    return this.children.autoHeight.getView();
  }
};

export const TimeLineComp = withExposingConfigs(TimeLineBasicComp, [
  new NameConfig("value", trans("timeLine.valueDesc")),
  new NameConfig("clickedObject", trans("timeLine.clickedObjectDesc")),
  new NameConfig("clickedIndex", trans("timeLine.clickedIndexDesc")),
  NameConfigHidden,
]);
