import { getDayJSLocale } from "@barda/i18n/dayjsLocale";
import { InputRef } from "antd";
import { default as AntAutoComplete } from "antd/es/auto-complete";
import { default as AntInput } from "antd/es/input";
import { BaseOptionType } from "antd/es/select";
import { Input, Section, sectionNames } from "barda-design";
import { BoolControl } from "comps/controls/boolControl";
import { jsonControl } from "comps/controls/codeControl";
import { booleanExposingStateControl, jsonObjectExposingStateControl } from "comps/controls/codeStateControl";
import { dropdownControl } from "comps/controls/dropdownControl";
import { IconControl } from "comps/controls/iconControl";
import { RefControl } from "comps/controls/refControl";
import { styleControl } from "comps/controls/styleControl";
import { InputLikeStyle, InputLikeStyleType } from "comps/controls/styleControlConstants";
import {
  NameConfig,
  NameConfigPlaceHolder,
  NameConfigRequired,
  withExposingConfigs,
} from "comps/generators/withExposing";
import { hasIcon } from "comps/utils";
import { allowClearPropertyView, hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import _ from "lodash";
import { pinyin } from "pinyin-pro";
import { useCallback, useEffect, useMemo, useState } from "react";
import styled from "styled-components";
import { UICompBuilder, withDefault } from "../../generators";
import { withMethodExposing } from "../../generators/withMethodExposing";
import { FormDataPropertyView } from "../formComp/formDataConstants";
import { AllPinyinOption, FirstPinyinOption } from "../selectInputComp/selectCompConstants";
import {
  TextInputBasicSection,
  textInputChildren,
  TextInputConfigs,
  TextInputInteractionSection,
  textInputValidate,
  TextInputValidationSection,
} from "../textInputComp/textInputConstants";
import {
  autoCompleteDate,
  autocompleteIconColor,
  autoCompleteRefMethods,
  autoCompleteType,
  convertAutoCompleteData,
  itemsDataTooltip,
  valueOrLabelOption,
} from "./autoCompleteConstants";

const SearchStyle = styled(AntInput.Search)<{ $style: InputLikeStyleType }>`
  .ant-input-affix-wrapper {
    background-color: ${(props) => props.$style.background};
    color: ${(props) => props.$style.text};
    border-color: ${(props) => props.$style.border};
    border-radius: ${(props) => props.$style.radius} 0 0 ${(props) => props.$style.radius}!important;

    &:hover {
      border-color: ${(props) => props.$style.accent};
    }

    &:focus,
    &.ant-input-affix-wrapper-focused {
      border-color: ${(props) => props.$style.accent};
    }
  }
  button {
    border-radius: 0 ${(props) => props.$style.radius} ${(props) => props.$style.radius} 0 !important;
  }
`;

const InputStyle = styled(Input)<{ $style: InputLikeStyleType }>`
  border-radius: ${(props) => props.$style.radius};

  &:not(.ant-input-disabled, .ant-input-affix-wrapper-disabled),
  input {
    color: ${(props) => props.$style.text};
    background-color: ${(props) => props.$style.background};
    border-color: ${(props) => props.$style.border};

    &:focus,
    &.ant-input-affix-wrapper-focused {
      border-color: ${(props) => props.$style.accent};
    }

    &:hover {
      border-color: ${(props) => props.$style.accent};
    }

    &::-webkit-input-placeholder {
      color: ${(props) => props.$style.text};
      opacity: 0.4;
    }

    .ant-input-show-count-suffix,
    .ant-input-prefix,
    .ant-input-suffix svg {
      opacity: 0.45;
      color: ${(props) => props.$style.text};
    }

    .ant-input-clear-icon svg:hover {
      opacity: 0.65;
    }
  }
`;

const AutoCompleteStyle = styled(AntAutoComplete)<{ $style: InputLikeStyleType }>`
  width: 100%;
  height: auto;
`;

const childrenMap = {
  ...textInputChildren,
  viewRef: RefControl<InputRef>,
  allowClear: BoolControl.DEFAULT_TRUE,
  style: withDefault(styleControl(InputLikeStyle), {}),
  prefixIcon: IconControl,
  suffixIcon: IconControl,
  items: jsonControl(convertAutoCompleteData, autoCompleteDate),
  ignoreCase: BoolControl.DEFAULT_TRUE,
  searchFirstPY: BoolControl.DEFAULT_TRUE,
  searchCompletePY: BoolControl,
  searchLabelOnly: BoolControl.DEFAULT_TRUE,
  valueOrLabel: dropdownControl(valueOrLabelOption, "label"),
  autoCompleteType: dropdownControl(autoCompleteType, "normal"),
  autocompleteIconColor: dropdownControl(autocompleteIconColor, "blue"),
  valueInItems: booleanExposingStateControl("valueInItems"),
  selectObject: jsonObjectExposingStateControl("selectObject", {}),
};

const getValidate = (value: any): "" | "warning" | "error" | undefined => {
  if (value.hasOwnProperty("validateStatus") && value["validateStatus"] === "error") return "error";
  return "";
};

let AutoCompleteCompBase = (function () {
  return new UICompBuilder(childrenMap, (props) => {
    const {
      items,
      placeholder,
      searchFirstPY,
      searchCompletePY,
      searchLabelOnly,
      ignoreCase,
      valueOrLabel,
      autoCompleteType,
      autocompleteIconColor,
    } = props;

    const getTextInputValidate = useCallback(() => {
      return {
        value: { value: props.value.value },
        required: props.required,
        minLength: props?.minLength ?? 0,
        maxLength: props?.maxLength ?? 0,
        validationType: props.validationType,
        regex: props.regex,
        customRule: props.customRule,
      };
    }, [
      props.value.value,
      props.required,
      props?.minLength,
      props?.maxLength,
      props.validationType,
      props.regex,
      props.customRule,
    ]);

    const [activationFlag, setActivationFlag] = useState(false);
    const [searchtext, setsearchtext] = useState<string>(props.value.value);
    const [validateState, setvalidateState] = useState({});
    const [PYCache, setPYCache] = useState({});

    //   是否中文环境
    const [chineseEnv] = useState(getDayJSLocale() === "zh-cn");
    useEffect(() => {
      props.value.onChange(props.defaultValue.value);
    }, [props.defaultValue.value]);

    useEffect(() => {
      setsearchtext(props.value.value);
      activationFlag && setvalidateState(textInputValidate(getTextInputValidate()));
    }, [
      props.value.value,
      props.required,
      props?.minLength,
      props?.maxLength,
      props.validationType,
      props.regex,
      props.customRule,
      activationFlag,
      getTextInputValidate,
    ]);

    useEffect(() => {
      var temp = _.reduce(
        props.items,
        (obj: any, item: any) => {
          if (item?.value)
            obj[item.value] = {
              first: pinyin(item!.value, FirstPinyinOption),
              all: pinyin(item!.value, AllPinyinOption),
            };
          if (item?.label)
            obj[item.label] = {
              first: pinyin(item!.label, FirstPinyinOption),
              all: pinyin(item!.label, AllPinyinOption),
            };
          return obj;
        },
        {}
      );
      setPYCache(temp);
    }, [props.items]);

    const debouncedSubmit = useMemo(
      () => _.debounce(() => props.onEvent("submit"), 100),
      [props.onEvent]
    );

    const onChange = (value: unknown) => {
      props.valueInItems.onChange(false);
      setvalidateState(textInputValidate(getTextInputValidate()));
      setsearchtext(value as string);
      props.value.onChange(value as string);
      props.onEvent("change");
    };
    const onFocus = () => {
      setActivationFlag(true);
      props.onEvent("focus");
    };
    const onBlur = () => {
      props.onEvent("blur");
    };
    const onSelect = (data: unknown, option: BaseOptionType) => {
      setsearchtext(option[valueOrLabel]);
      props.valueInItems.onChange(true);
      props.value.onChange(option[valueOrLabel]);
      props.selectObject.onChange(option);
      debouncedSubmit();
    };

    const onPressEnter = () => {
      debouncedSubmit();
    };

    const filterOption = (
      inputValue: string,
      option?: BaseOptionType
    ): boolean => {
      var InputValueLowerCase = inputValue.toLowerCase();
      if (ignoreCase) {
        if (option!.label.toLowerCase().indexOf(InputValueLowerCase) !== -1) return true;
      } else {
        if (option!.label.indexOf(inputValue) !== -1) return true;
      }
      if (
        chineseEnv &&
        searchFirstPY &&
        _.get(PYCache, `${option!.label}.first`, "").indexOf(InputValueLowerCase) >= 0
      )
        return true;
      if (
        chineseEnv &&
        searchCompletePY &&
        _.get(PYCache, `${option!.label}.all`, "").indexOf(InputValueLowerCase) >= 0
      )
        return true;
      if (!searchLabelOnly) {
        if (ignoreCase) {
          if (option!.value.toLowerCase().indexOf(InputValueLowerCase) !== -1) return true;
        } else {
          if (option!.value.indexOf(inputValue) !== -1) return true;
        }
        if (
          chineseEnv &&
          searchFirstPY &&
          _.get(PYCache, `${option!.value}.first`, "").indexOf(InputValueLowerCase) >= 0
        )
          return true;
        if (
          chineseEnv &&
          searchCompletePY &&
          _.get(PYCache, `${option!.value}.all`, "").indexOf(InputValueLowerCase) >= 0
        )
          return true;
      }
      return false;
    };

    return props.label({
      required: props.required,
      children: (
        <AutoCompleteStyle
          $style={props.style}
          disabled={props.disabled}
          value={searchtext}
          options={items}
          onChange={onChange}
          onFocus={onFocus}
          onBlur={onBlur}
          onSelect={onSelect}
          filterOption={filterOption}
        >
          {autoCompleteType === "search" ? (
            <SearchStyle
              $style={props.style}
              placeholder={placeholder}
              enterButton={autocompleteIconColor === "blue"}
              allowClear={props.allowClear}
              ref={props.viewRef}
              onPressEnter={onPressEnter}
              status={getValidate(validateState)}
              onSearch={(value) => {
                if (value.length > 0) {
                  debouncedSubmit();
                }
              }}
            />
          ) : (
            <InputStyle
              ref={props.viewRef}
              placeholder={placeholder}
              allowClear={props.allowClear}
              $style={props.style}
              prefix={hasIcon(props.prefixIcon) && props.prefixIcon}
              suffix={hasIcon(props.suffixIcon) && props.suffixIcon}
              status={getValidate(validateState)}
              onPressEnter={onPressEnter}
            />
          )}
        </AutoCompleteStyle>
      ),
      style: props.style,
      ...validateState,
    });
  })
    .setPropertyViewFn((children) => {
      return (
        <>
          <Section name={trans("autoComplete.ComponentType")}>
            {children.autoCompleteType.propertyView({
              label: trans("autoComplete.type"),
              radioButton: true,
            })}
            {children.autoCompleteType.getView() === "search" &&
              children.autocompleteIconColor.propertyView({
                label: trans("button.prefixIcon"),
                radioButton: true,
              })}

            {children.autoCompleteType.getView() === "normal" &&
              children.prefixIcon.propertyView({
                label: trans("button.prefixIcon"),
              })}
            {children.autoCompleteType.getView() === "normal" &&
              children.suffixIcon.propertyView({
                label: trans("button.suffixIcon"),
              })}
          </Section>
          <Section name={trans("autoComplete.SectionDataName")}>
            {children.items.propertyView({
              label: trans("autoComplete.value"),
              tooltip: itemsDataTooltip,
              placeholder: "[]",
            })}
            {getDayJSLocale() === "zh-cn" &&
              children.searchFirstPY.propertyView({
                label: trans("autoComplete.searchFirstPY"),
              })}
            {getDayJSLocale() === "zh-cn" &&
              children.searchCompletePY.propertyView({
                label: trans("autoComplete.searchCompletePY"),
              })}
            {children.searchLabelOnly.propertyView({
              label: trans("autoComplete.searchLabelOnly"),
            })}
            {children.ignoreCase.propertyView({
              label: trans("autoComplete.ignoreCase"),
            })}
            {children.valueOrLabel.propertyView({
              label: trans("autoComplete.checkedValueFrom"),
              radioButton: true,
            })}
            {allowClearPropertyView(children)}
          </Section>
          <TextInputBasicSection {...children} />

          <FormDataPropertyView {...children} />
          {children.label.getPropertyView()}

          <TextInputInteractionSection {...children} />

          {<TextInputValidationSection {...children} />}

          <Section name={sectionNames.layout}>{hiddenPropertyView(children)}</Section>

          <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
        </>
      );
    })
    .setExposeMethodConfigs(autoCompleteRefMethods)
    .setExposeStateConfigs([
      new NameConfig("value", trans("export.inputValueDesc")),
      new NameConfig("valueInItems", trans("autoComplete.valueInItems")),
      NameConfigPlaceHolder,
      NameConfigRequired,
      ...TextInputConfigs,
    ])
    .build();
})();

AutoCompleteCompBase = class extends AutoCompleteCompBase {
  override autoHeight(): boolean {
    return true;
  }
};

let AutoCompleteCompWithMethods = withMethodExposing(AutoCompleteCompBase, [
  {
    method: {
      name: "submit",
      description: trans("method.submit"),
      params: [],
    },
    execute: (comp, values) => {
      comp.children.onEvent.getView()("submit");
      return Promise.resolve();
    },
  },
]);

export const AutoCompleteComp = withExposingConfigs(AutoCompleteCompWithMethods, [
  new NameConfig("value", trans("export.inputValueDesc")),
  new NameConfig("selectObject", trans("autoComplete.selectObjectDesc")),
  new NameConfig("valueInItems", trans("autoComplete.valueInItems")),
  NameConfigPlaceHolder,
  NameConfigRequired,
  ...TextInputConfigs,
]);
