import { Statistic } from "antd";
import { RecordConstructorToView } from "barda-core";
import { Section, sectionNames } from "barda-design";
import { BoolControl } from "comps/controls/boolControl";
import { NumberControl, RangeControl, StringControl } from "comps/controls/codeControl";
import { ButtonEventHandlerControl } from "comps/controls/eventHandlerControl";
import { IconControl } from "comps/controls/iconControl";
import { styleControl } from "comps/controls/styleControl";
import { StatisticCardStyle, StatisticCardStyleType } from "comps/controls/styleControlConstants";
import { withDefault } from "comps/generators";
import { UICompBuilder } from "comps/generators/uiCompBuilder";
import { NameConfig, NameConfigHidden, withExposingConfigs } from "comps/generators/withExposing";
import { hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import React, { Suspense } from "react";
import styled from "styled-components";
import { hasIcon } from "../utils";

const StatisticCardWrapper = styled.div<{
  $style: StatisticCardStyleType;
  $clickable: boolean;
}>`
  width: 100%;
  height: 100%;
  padding: 16px;
  background: ${(props) => props.$style.background};
  border-radius: ${(props) => props.$style.radius};
  border: 1px solid ${(props) => props.$style.border};
  cursor: ${(props) => (props.$clickable ? "pointer" : "default")};
  transition: all 0.2s;

  ${(props) =>
    props.$clickable &&
    `
    &:hover {
      ${props.$style.hoverBackground ? `background: ${props.$style.hoverBackground} !important;` : ""}
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  `}

  .statistic-card-content {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }

  .statistic-card-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: ${(props) => props.$style.iconBackground};

    svg {
      width: 32px !important;
      height: 32px !important;
      color: #fff;
    }

    img {
      width: 32px;
      height: 32px;
      object-fit: contain;
    }
  }

  .statistic-card-info {
    flex: 1;
    min-width: 0;
  }

  .ant-statistic-title {
    font-size: ${(props) => props.$style.titleFontSize_UNIT} !important;
    color: ${(props) => props.$style.titleColor} !important;
    margin-bottom: 8px;
  }

  .ant-statistic-content {
    font-size: ${(props) => props.$style.valueFontSize_UNIT} !important;
    color: ${(props) => props.$style.valueColor} !important;
  }
`;

// 懒加载 CountUp 组件，只有在启用动画时才加载
const CountUp = React.lazy(() => import("react-countup"));

const childrenMap = {
  title: withDefault(StringControl, trans("statisticCard.title")),
  value: withDefault(NumberControl, 0),
  prefix: withDefault(StringControl, ""),
  suffix: withDefault(StringControl, ""),
  precision: RangeControl.closed(0, 20, 0),
  icon: IconControl,
  enableAnimation: withDefault(BoolControl, false),
  onEvent: ButtonEventHandlerControl,
  style: styleControl(StatisticCardStyle),
};

// 动画值组件
const AnimatedValue = (props: {
  value: number;
  enableAnimation: boolean;
  precision: number;
  valueStyle: React.CSSProperties;
}) => {
  const formattedValue = props.precision > 0 ? props.value.toFixed(props.precision) : props.value;

  if (!props.enableAnimation) {
    return <>{formattedValue}</>;
  }

  return (
    <Suspense fallback={<>{formattedValue}</>}>
      <CountUp
        start={0}
        end={props.value}
        duration={2}
        decimals={props.precision}
        separator=","
        style={props.valueStyle}
      />
    </Suspense>
  );
};

const StatisticCardView = (
  props: RecordConstructorToView<typeof childrenMap> & { $hasClickHandler?: boolean }
) => {
  return (
    <StatisticCardWrapper
      $style={props.style}
      $clickable={props.$hasClickHandler ?? false}
      onClick={() => {
        props.onEvent?.("click");
      }}
    >
      <div className="statistic-card-content">
        <div className="statistic-card-info">
          <Statistic
            title={props.title}
            value={props.value}
            prefix={props.prefix || undefined}
            suffix={props.suffix || undefined}
            precision={props.precision}
            formatter={(value: string | number) => {
              const numericValue = typeof value === "number" ? value : Number(value) || 0;
              if (props.enableAnimation) {
                return (
                  <AnimatedValue
                    value={numericValue}
                    enableAnimation={props.enableAnimation}
                    precision={props.precision}
                    valueStyle={{ color: props.style.valueColor }}
                  />
                );
              }
              // 应用精度格式化
              return props.precision > 0 ? numericValue.toFixed(props.precision) : numericValue;
            }}
            valueStyle={{ color: props.style.valueColor }}
          />
        </div>
        {hasIcon(props.icon) && <div className="statistic-card-icon">{props.icon}</div>}
      </div>
    </StatisticCardWrapper>
  );
};

let StatisticCardBasicComp = (function () {
  return new UICompBuilder(
    childrenMap,
    (props: RecordConstructorToView<typeof childrenMap>, dispatch: any, comp?: any) => {
      // 通过 comp.children 访问原始的 control 对象，判断是否绑定了 click 事件
      const hasClickHandler = (comp?.children?.onEvent as any)?.isBind?.("click") ?? false;
      return <StatisticCardView {...props} $hasClickHandler={hasClickHandler} />;
    }
  )
    .setPropertyViewFn((children) => (
      <>
        <Section name={sectionNames.basic}>
          {children.title.propertyView({
            label: trans("statisticCard.title"),
            tooltip: trans("statisticCard.titleTooltip"),
          })}
          {children.value.propertyView({
            label: trans("statisticCard.value"),
            tooltip: trans("statisticCard.valueTooltip"),
          })}
          {children.prefix.propertyView({
            label: trans("statisticCard.prefix"),
            tooltip: trans("statisticCard.prefixTooltip"),
          })}
          {children.suffix.propertyView({
            label: trans("statisticCard.suffix"),
            tooltip: trans("statisticCard.suffixTooltip"),
          })}
          {children.precision.propertyView({
            label: trans("statisticCard.precision"),
            tooltip: trans("statisticCard.precisionTooltip"),
          })}
          {children.icon.propertyView({
            label: trans("statisticCard.icon"),
            tooltip: trans("statisticCard.iconTooltip"),
          })}
          {children.enableAnimation.propertyView({
            label: trans("statisticCard.enableAnimation"),
            tooltip: trans("statisticCard.enableAnimationTooltip"),
          })}
        </Section>
        <Section name={sectionNames.interaction}>{children.onEvent.propertyView()}</Section>
        <Section name={sectionNames.layout}>{hiddenPropertyView(children)}</Section>
        <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
      </>
    ))
    .build();
})();

export const StatisticCardComp = withExposingConfigs(StatisticCardBasicComp, [
  NameConfigHidden,
]);
