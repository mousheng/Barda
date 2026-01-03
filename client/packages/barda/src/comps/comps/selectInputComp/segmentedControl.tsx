import { Segmented as AntdSegmented, Badge } from "antd";
import { Section, sectionNames } from "barda-design";
import { BoolCodeControl } from "comps/controls/codeControl";
import { stringExposingStateControl } from "comps/controls/codeStateControl";
import { dropdownControl } from "comps/controls/dropdownControl";
import { ChangeEventHandlerControl } from "comps/controls/eventHandlerControl";
import { LabelControl } from "comps/controls/labelControl";
import { SelectOptionControl } from "comps/controls/optionsControl";
import { RefControl } from "comps/controls/refControl";
import { styleControl } from "comps/controls/styleControl";
import { SegmentStyle, SegmentStyleType } from "comps/controls/styleControlConstants";
import { hasIcon } from "comps/utils";
import { disabledPropertyView, hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import styled, { css } from "styled-components";
import { UICompBuilder } from "../../generators";
import { CommonNameConfig, NameConfig, withExposingConfigs } from "../../generators/withExposing";
import { formDataChildren, FormDataPropertyView } from "../formComp/formDataConstants";
import {
  selectDivRefMethods,
  SelectInputInvalidConfig,
  SelectInputValidationChildren,
  SelectInputValidationSection,
  useSelectInputValidate,
} from "./selectInputConstants";

export const SegmentedLayoutOptions = [
  { label: trans("radio.horizontal"), value: "horizontal" },
  { label: trans("radio.vertical"), value: "vertical" },
] as const;

const getStyle = (style: SegmentStyleType) => {
  return css`
    &.ant-segmented:not(.ant-segmented-disabled) {
      background-color: ${style.background};

      &,
      .ant-segmented-item-selected,
      .ant-segmented-thumb,
      .ant-segmented-item:hover,
      .ant-segmented-item:focus {
        color: ${style.text};
        border-radius: ${style.radius};
      }

      .ant-segmented-item-selected,
      .ant-segmented-thumb {
        background-color: ${style.indicatorBackground};
      }
    }

    &.ant-segmented,
    .ant-segmented-item-selected {
      border-radius: ${style.radius};
    }
  `;
};

const Segmented = styled(AntdSegmented) <{ $style: SegmentStyleType; $layout?: "horizontal" | "vertical" }>`
  width: 100%;
  height: ${(props) => (props.$layout === "vertical" ? "auto" : "32px")};
  ${(props) => props.$style && getStyle(props.$style)}
`;

const SegmentChildrenMap = {
  defaultValue: stringExposingStateControl("value"),
  value: stringExposingStateControl("value"),
  label: LabelControl,
  disabled: BoolCodeControl,
  onEvent: ChangeEventHandlerControl,
  options: SelectOptionControl,
  style: styleControl(SegmentStyle),
  layout: dropdownControl(SegmentedLayoutOptions, "horizontal"),
  viewRef: RefControl<HTMLDivElement>,

  ...SelectInputValidationChildren,
  ...formDataChildren,
};

const SegmentedControlBasicComp = (function () {
  return new UICompBuilder(SegmentChildrenMap, (props) => {
    const {
      validateState,
      handleChange,
   } = useSelectInputValidate(props);

    return props.label({
      required: props.required,
      style: props.style,
      children: (
        <Segmented
          vertical={props.layout === "vertical"}
          ref={props.viewRef}
          block
          disabled={props.disabled}
          value={props.value.value}
          $style={props.style}
          $layout={props.layout}
          onChange={(value: any) => handleChange(value.toString())}
          options={props.options
            .filter((option) => option.value !== undefined && !option.hidden)
            .map((option) => ({
              label: option.badge > 0 ? (
                <Badge count={option.badge} size="small" offset={[5, 0]}>
                  {option.label}
                </Badge>
              ) : (
                option.label
              ),
              value: option.value,
              disabled: option.disabled,
              icon: hasIcon(option.prefixIcon) && option.prefixIcon,
            }))}
        />
      ),
      ...validateState,
    });
  })
    .setPropertyViewFn((children) => (
      <>
        <Section name={sectionNames.basic}>
          {children.options.propertyView({ showBadge: true })}
          {children.defaultValue.propertyView({ label: trans("prop.defaultValue") })}
        </Section>
        <FormDataPropertyView {...children} />
        {children.label.getPropertyView()}

        <Section name={sectionNames.interaction}>
          {children.onEvent.getPropertyView()}
          {disabledPropertyView(children)}
        </Section>

        <SelectInputValidationSection {...children} />

        <Section name={sectionNames.layout}>
          {children.layout.propertyView({
            label: trans("segmented.layout"),
            radioButton: true,
          })}
          {hiddenPropertyView(children)}
        </Section>
        <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
      </>
    ))
    .setExposeMethodConfigs(selectDivRefMethods)
    .build();
})();

export const SegmentedControlComp = withExposingConfigs(SegmentedControlBasicComp, [
  new NameConfig("value", trans("selectInput.valueDesc")),
  SelectInputInvalidConfig,
  ...CommonNameConfig,
]);
