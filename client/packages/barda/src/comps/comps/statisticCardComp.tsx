import { Statistic } from "antd";
import { RecordConstructorToView } from "barda-core";
import { Section, sectionNames } from "barda-design";
import { dropdownControl } from "comps/controls/dropdownControl";
import { BoolControl } from "comps/controls/boolControl";
import { NumberControl, RangeControl, StringControl } from "comps/controls/codeControl";
import { ButtonEventHandlerControl } from "comps/controls/eventHandlerControl";
import { IconControl } from "comps/controls/iconControl";
import { styleControl } from "comps/controls/styleControl";
import { StatisticCardStyle, StatisticCardStyleType } from "comps/controls/styleControlConstants";
import { withDefault } from "comps/generators";
import { UICompBuilder } from "comps/generators/uiCompBuilder";
import { NameConfigHidden, withExposingConfigs } from "comps/generators/withExposing";
import { hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import React, { Suspense } from "react";
import styled from "styled-components";
import { hasIcon } from "../utils";

const sizeOptions = [
  { label: trans("statisticCard.sizeNormal"), value: "normal" },
  { label: trans("statisticCard.sizeCompact"), value: "compact" },
] as const;

type StatisticCardSize = typeof sizeOptions[number]["value"];

const StatisticCardWrapper = styled.div<{
  $style: StatisticCardStyleType;
  $clickable: boolean;
  $size: StatisticCardSize;
  $padding: string;
}>`
  width: 100%;
  height: 100%;
  padding: ${(props) => props.$padding};
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
    align-items: center;
    // gap: ${(props) => (props.$size === "compact" ? "12px" : "16px")};
  }

  .statistic-card-icon {
    width: ${(props) => (props.$size === "compact" ? "40px" : "48px")};
    height: ${(props) => (props.$size === "compact" ? "40px" : "48px")};
    border-radius: ${(props) => (props.$size === "compact" ? "6px" : "8px")};
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: ${(props) => props.$style.iconBackground};

    svg {
      width: ${(props) => (props.$size === "compact" ? "28px" : "32px")} !important;
      height: ${(props) => (props.$size === "compact" ? "28px" : "32px")} !important;
      color: #fff;
    }

    img {
      width: ${(props) => (props.$size === "compact" ? "28px" : "32px")};
      height: ${(props) => (props.$size === "compact" ? "28px" : "32px")};
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
    margin-bottom: ${(props) => (props.$size === "compact" ? "6px" : "8px")};
  }

  .statistic-card-values .ant-statistic-title {
    display: none;
  }

  .ant-statistic-content {
    line-height: 1;
    font-size: ${(props) => props.$style.valueFontSize_UNIT};
    color: ${(props) => props.$style.valueColor};
  }

  .statistic-card-values {
    display: flex;
    align-items: baseline;
    flex-wrap: wrap;
    gap: ${(props) => (props.$size === "compact" ? "4px" : "6px")};
  }

  .statistic-card-values .ant-statistic-content {
    white-space: nowrap;
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
  showSecondaryIndicator: withDefault(BoolControl, false),
  secondaryValue: withDefault(NumberControl, 0),
  secondaryPrefix: withDefault(StringControl, ""),
  secondarySuffix: withDefault(StringControl, ""),
  secondaryPrecision: RangeControl.closed(0, 20, 0),
  size: dropdownControl(sizeOptions, "normal"),
  icon: IconControl,
  enableAnimation: withDefault(BoolControl, false),
  onEvent: ButtonEventHandlerControl,
  style: styleControl(StatisticCardStyle),
  duration: withDefault(NumberControl, 1.5),
};

// 动画值组件
const AnimatedValue = (props: {
  value: number;
  enableAnimation: boolean;
  precision: number;
  valueStyle: React.CSSProperties;
  duration: number;
}) => {
  const formattedValue = props.precision > 0 ? props.value.toFixed(props.precision) : props.value;

  if (!props.enableAnimation) {
    return <>{formattedValue}</>;
  }

  return (
    <Suspense fallback={<span style={props.valueStyle}>{formattedValue}</span>}>
      <CountUp
        start={0}
        end={props.value}
        duration={props.duration}
        decimals={props.precision}
        separator=","
        style={props.valueStyle}
      />
    </Suspense>
  );
};

const renderAffix = (text: string, style: React.CSSProperties) => {
  return text ? <span style={style}>{text}</span> : undefined;
};

const getNumericValue = (value: string | number) => {
  return typeof value === "number" ? value : Number(value) || 0;
};

const formatStatisticValue = (value: string | number, precision: number) => {
  const numericValue = getNumericValue(value);
  return precision > 0 ? numericValue.toFixed(precision) : numericValue;
};

const StatisticCardView = (props: RecordConstructorToView<typeof childrenMap> & { $hasClickHandler?: boolean }) => {
  const customPadding = props.style.padding_UNIT?.trim();
  const padding = customPadding && customPadding.length > 0 ? customPadding : props.size === "compact" ? "9px" : "16px";
  const primaryPrefixStyle = { color: props.style.prefixColor || props.style.valueColor, fontSize: props.style.valueFontSize_UNIT };
  const primarySuffixStyle = { color: props.style.suffixColor || props.style.valueColor, fontSize: props.style.valueFontSize_UNIT };
  const secondaryValueStyle = { color: props.style.secondaryValueColor, fontSize: props.style.secondaryValueFontSize_UNIT };
  const secondaryPrefixStyle = {
    color: props.style.secondaryPrefixColor || props.style.secondaryValueColor,
    fontSize: props.style.secondaryValueFontSize_UNIT,
  };
  const secondarySuffixStyle = {
    color: props.style.secondarySuffixColor || props.style.secondaryValueColor,
    fontSize: props.style.secondaryValueFontSize_UNIT,
  };

  const renderValue = (value: string | number, precision: number, valueStyle: React.CSSProperties) => {
    const numericValue = getNumericValue(value);
    if (props.enableAnimation) {
      return (
        <AnimatedValue
          value={numericValue}
          enableAnimation={props.enableAnimation}
          precision={precision}
          valueStyle={valueStyle}
          duration={props.duration}
        />
      );
    }
    return formatStatisticValue(numericValue, precision);
  };

  return (
    <StatisticCardWrapper
      $style={props.style}
      $clickable={props.$hasClickHandler ?? false}
      $size={props.size}
      $padding={padding}
      onClick={() => {
        props.onEvent?.("click");
      }}
    >
      <div className="statistic-card-content">
        <div className="statistic-card-info">
          <Statistic title={props.title} value={undefined} formatter={() => undefined} />
          <div className="statistic-card-values">
            <Statistic
              value={props.value}
              prefix={renderAffix(props.prefix, primaryPrefixStyle)}
              suffix={renderAffix(props.suffix, primarySuffixStyle)}
              precision={props.precision}
              formatter={(value: string | number) =>
                renderValue(value, props.precision, {
                  color: props.style.valueColor,
                  fontSize: props.style.valueFontSize_UNIT,
                })
              }
              styles={{ content: { color: props.style.valueColor, fontSize: props.style.valueFontSize_UNIT } }}
            />
            {props.showSecondaryIndicator && (
              <Statistic
                className="statistic-card-secondary"
                value={props.secondaryValue}
                prefix={renderAffix(props.secondaryPrefix, secondaryPrefixStyle)}
                suffix={renderAffix(props.secondarySuffix, secondarySuffixStyle)}
                precision={props.secondaryPrecision}
                formatter={(value: string | number) => renderValue(value, props.secondaryPrecision, secondaryValueStyle)}
                styles={{ content: secondaryValueStyle }}
              />
            )}
          </div>
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
          {children.size.propertyView({
            label: trans("statisticCard.size"),
            tooltip: trans("statisticCard.sizeTooltip"),
            radioButton: true,
          })}
          {children.icon.propertyView({
            label: trans("statisticCard.icon"),
            tooltip: trans("statisticCard.iconTooltip"),
          })}
          {children.enableAnimation.propertyView({
            label: trans("statisticCard.enableAnimation"),
            tooltip: trans("statisticCard.enableAnimationTooltip"),
          })}
          {children.enableAnimation.getView() &&
            children.duration.propertyView({
              label: trans("statisticCard.duration"),
              tooltip: trans("statisticCard.durationTooltip"),
            })}
        </Section>
        <Section name={trans("statisticCard.secondaryIndicator")}>
          {children.showSecondaryIndicator.propertyView({
            label: trans("statisticCard.showSecondaryIndicator"),
            tooltip: trans("statisticCard.showSecondaryIndicatorTooltip"),
          })}
          {children.showSecondaryIndicator.getView() && [
            children.secondaryValue.propertyView({
              label: trans("statisticCard.secondaryValue"),
              tooltip: trans("statisticCard.secondaryValueTooltip"),
            }),
            children.secondaryPrefix.propertyView({
              label: trans("statisticCard.secondaryPrefix"),
              tooltip: trans("statisticCard.secondaryPrefixTooltip"),
            }),
            children.secondarySuffix.propertyView({
              label: trans("statisticCard.secondarySuffix"),
              tooltip: trans("statisticCard.secondarySuffixTooltip"),
            }),
            children.secondaryPrecision.propertyView({
              label: trans("statisticCard.secondaryPrecision"),
              tooltip: trans("statisticCard.secondaryPrecisionTooltip"),
            }),
          ]}
        </Section>
        <Section name={sectionNames.interaction}>{children.onEvent.propertyView()}</Section>
        <Section name={sectionNames.layout}>{hiddenPropertyView(children)}</Section>
        <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
      </>
    ))
    .build();
})();

export const StatisticCardComp = withExposingConfigs(StatisticCardBasicComp, [NameConfigHidden]);
