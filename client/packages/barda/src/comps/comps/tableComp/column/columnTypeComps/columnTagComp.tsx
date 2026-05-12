import { ActionSelectorControl } from "@barda/comps/controls/actionSelector/actionSelectorControl";
import { updateActionContextAction } from "barda-core";
import { Tag } from "antd";
import { CustomSelect, PackUpIcon, ScrollBar } from "barda-design";
import { TagsContext } from "components/table/EditableCell";
import { ColorMapOptionControl } from "comps/comps/selectInputComp/selectCompConstants";
import {
  ColumnTypeCompBuilder,
  ColumnTypeViewFn,
} from "comps/comps/tableComp/column/columnTypeCompBuilder";
import { ColumnValueTooltip } from "comps/comps/tableComp/column/simpleColumnTypeComps";
import { BoolControl } from "comps/controls/boolControl";
import { codeControl } from "comps/controls/codeControl";
import { trans } from "i18n";
import _ from "lodash";
import { ReactNode, useContext, useMemo, useState } from "react";
import { toJson } from "really-relaxed-json";
import { JSONObject } from "util/jsonTypes";
import { ColumnEditableContext } from "./columnTagsComp";
import {
  ColorMapContext,
  DEFAULT_COLOR_KEY,
  DropdownStyled,
  deriveTagPalette,
  getIconFromOptions,
  getTagColor,
  isStringArray,
  toColorMap,
  Wrapper,
} from "./columnTypeUtils/tagUtils";

/* ------------------------------ 工具函数区 ------------------------------ */

// 解析 value 为字符串（Tag单选专用）
// 如果是数组，合并为逗号分隔的字符串
export const parseTagData = (value: unknown): string => {
  if (isStringArray(value)) {
    return value.map(String).join(",");
  }

  if (["string", "number", "boolean"].includes(typeof value)) {
    if (typeof value === "string") {
      try {
        const result = JSON.parse(toJson(value));
        if (isStringArray(result)) {
          return result.map(String).join(",");
        }
      } catch (_) {}
    }
    return String(value);
  }

  return "";
};

/* ------------------------------ 控件配置 ------------------------------ */

// Tag单选控件：接受 string | string[]，统一转为字符串
const TagControl = codeControl<string>(parseTagData, {
  expectedType: "string",
  codeType: "JSON",
});

const childrenMap = {
  text: TagControl,
  colorMap: ColorMapOptionControl,
  allowCustomTags: BoolControl,
  onTagClick: ActionSelectorControl,
  allowClear: BoolControl,
};

/* ------------------------------ 数据基础转换 ------------------------------ */

const getBaseValue: ColumnTypeViewFn<
  typeof childrenMap,
  { text: string; colorMap?: JSONObject; colorMapOptions?: any[]; allowCustomTags?: boolean },
  { text: string; colorMap?: JSONObject; colorMapOptions?: any[]; allowCustomTags?: boolean }
> = (props) => {
  const colorMapOptions = props.colorMap;
  if (Array.isArray(colorMapOptions)) {
    return {
      text: props.text,
      colorMap: toColorMap(colorMapOptions),
      colorMapOptions: colorMapOptions,
      allowCustomTags: props.allowCustomTags,
    };
  }
  return { text: props.text, colorMap: colorMapOptions, allowCustomTags: props.allowCustomTags };
};

/* ------------------------------ 编辑组件 ------------------------------ */

type TagEditPropsType = {
  value: string;
  onChange: (value: string) => void;
  onChangeEnd: () => void;
  colorMap?: JSONObject;
  allowCustomTags?: boolean;
  allowClear?: boolean;
};

const TagEdit = (props: TagEditPropsType) => {
  const defaultTags = useContext(TagsContext);
  const contextValue = useContext(ColorMapContext);
  const mergedColorMap = props.colorMap || contextValue?.colorMap;
  const colorMapOptions = contextValue?.colorMapOptions;

  // 从 colorMapOptions 中提取有效的标签 label
  const colorMapLabels = useMemo(() => {
    if (!Array.isArray(colorMapOptions)) return [];
    return colorMapOptions
      .map(opt => opt?.label)
      .filter((label): label is string => typeof label === "string" && label !== DEFAULT_COLOR_KEY);
  }, [colorMapOptions]);

  // 生成所有可选标签（合并 defaultTags 和 colorMapLabels）
  const availableTags = useMemo(() => {
    const result: string[] = [];
    defaultTags.forEach((item) => {
      if (item.split(",")[1]) {
        item.split(",").forEach((tag) => result.push(tag));
      }
      result.push(item);
    });
    return [...new Set([...result, ...colorMapLabels])];
  }, [defaultTags, colorMapLabels]);

  const [tags, setTags] = useState(availableTags);
  const allowCustom = props.allowCustomTags !== false; // 默认允许自定义
  return (
    <Wrapper>
      <CustomSelect
        allowClear={props.allowClear}
        autoFocus
        defaultOpen
        variant="borderless"
        optionLabelProp="label"
        showSearch={allowCustom}
        defaultValue={props.value}
        style={{ width: "100%" }}
        suffixIcon={<PackUpIcon />}
        onSearch={(value) => {
          if (allowCustom) {
            if (availableTags.findIndex((item) => item.includes(value)) < 0) {
              setTags([...availableTags, value]);
            } else {
              setTags(availableTags);
            }
            props.onChange(value);
          }
        }}
        onChange={(value) => {
          props.onChange(value);
        }}
        popupRender={(originNode: ReactNode) => (
          <DropdownStyled>
            <ScrollBar style={{ maxHeight: "256px" }}>{originNode}</ScrollBar>
          </DropdownStyled>
        )}
        styles={{ popup: { root: { marginTop: "7px", padding: "8px 0 6px 0" } } }}
        onBlur={props.onChangeEnd}
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            props.onChangeEnd();
          }
        }}
      >
        {tags.map((value, index) => {
          const icon = getIconFromOptions(value, colorMapOptions);
          const tagColor = getTagColor(value, mergedColorMap);
          const palette = deriveTagPalette(tagColor);
          const tagStyle = palette
            ? {
                borderColor: palette.borderColor,
                backgroundColor: palette.backgroundColor,
                color: palette.textColor,
              }
            : undefined;
          const tagLabel = (
            <Tag color={palette ? undefined : tagColor} style={tagStyle} icon={icon}>
              {value}
            </Tag>
          );
          return (
            <CustomSelect.Option value={value} key={index} label={tagLabel}>
              {value}
            </CustomSelect.Option>
          );
        })}
      </CustomSelect>
    </Wrapper>
  );
};

/* ------------------------------ 列组件构建 ------------------------------ */

export const ColumnTagComp = (function () {
  return new ColumnTypeCompBuilder(
    childrenMap,
    (props, dispatch) => {
      // 获取最新的 colorMapOptions
      const latestColorMapOptions = props.colorMap as any;
      const latestColorMap = Array.isArray(latestColorMapOptions)
        ? toColorMap(latestColorMapOptions)
        : latestColorMapOptions;
      
      // 如果有 changeValue，合并最新的 colorMap 配置
      const baseValue = getBaseValue(props, dispatch);
      const value = props.changeValue 
        ? { ...props.changeValue, colorMap: latestColorMap, colorMapOptions: latestColorMapOptions }
        : baseValue;
      
      // 处理值：支持逗号分隔的字符串显示为多个tag
      let textValue: string | string[] = value.text;
      textValue = typeof textValue === "string" && textValue.split(",")[1] ? textValue.split(",") : textValue;
      const tags = _.isArray(textValue) ? textValue : [textValue];
      
      return tags.map((tag, index) => {
        const tagText = String(tag);
        const tagColor = getTagColor(tagText, latestColorMap);
        const palette = deriveTagPalette(tagColor);
        const tagStyle = palette
          ? {
              borderColor: palette.borderColor,
              backgroundColor: palette.backgroundColor,
              color: palette.textColor,
              cursor: "pointer",
            }
          : { cursor: "pointer", color: tagColor };
        const icon = getIconFromOptions(tagText, latestColorMapOptions);
        return (
          <Tag
            color={palette ? undefined : tagColor}
            style={tagStyle}
            icon={icon}
            key={index}
            onClick={() => {
              props.onTagClick({clickedTag: tag});
              dispatch(updateActionContextAction({ clickedTag: tag }));
            }}
          >
            {tagText}
          </Tag>
        );
      });
    },
    (nodeValue) => {
      // 序列化：Tag单选始终返回字符串
      const text = nodeValue.text.value;
      return typeof text === "string" ? text : String(text);
    },
    getBaseValue
  )
    .setEditViewFn((props) => {
      if (!props.value || typeof props.value !== 'object') return null;
      const value = props.value as { text: string; colorMap?: JSONObject; colorMapOptions?: any[]; allowCustomTags?: boolean };
      return (
        <ColorMapContext.Provider value={{ colorMap: value.colorMap, colorMapOptions: value.colorMapOptions }}>
          <TagEdit 
            value={value.text} 
            colorMap={value.colorMap}
            allowCustomTags={value.allowCustomTags}
            onChange={(text) => props.onChange({ text, colorMap: value.colorMap, colorMapOptions: value.colorMapOptions, allowCustomTags: value.allowCustomTags })} 
            onChangeEnd={props.onChangeEnd}
            allowClear={props.columnProps?.allowClear}
          />
        </ColorMapContext.Provider>
      );
    })
    .setPropertyViewFn((children) => {
      const columnEditable = useContext(ColumnEditableContext);
      return (
        <>
          {children.text.propertyView({
            label: trans("table.columnValue"),
            tooltip: ColumnValueTooltip,
          })}
          {children.colorMap.propertyView({
            title: trans("table.tagConfig"),
          })}
          {columnEditable &&
            children.allowCustomTags.propertyView({
              label: trans("table.allowCustomTags"),
              tooltip: trans("table.allowCustomTagsTooltip"),
            })}
          {
            children.allowClear.propertyView({
              label: trans("prop.showClear"),
            })
          }
          {children.onTagClick.propertyView({
            label: trans("table.onTagClick"),
          })}
        </>
      );
    })
    .build();
})();
