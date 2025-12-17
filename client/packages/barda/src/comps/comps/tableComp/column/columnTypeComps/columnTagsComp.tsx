import { Select, Tag } from "antd";
import { updateActionContextAction } from "barda-core";
import { ScrollBar } from "barda-design";
import { TagsContext } from "components/table/EditableCell";
import { ColorMapOptionControl } from "comps/comps/selectInputComp/selectCompConstants";
import { ColumnTypeCompBuilder, ColumnTypeViewFn } from "comps/comps/tableComp/column/columnTypeCompBuilder";
import { ColumnValueTooltip } from "comps/comps/tableComp/column/simpleColumnTypeComps";
import { ActionSelectorControl } from "comps/controls/actionSelector/actionSelectorControl";
import { BoolControl } from "comps/controls/boolControl";
import { codeControl } from "comps/controls/codeControl";
import { trans } from "i18n";
import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { toJson } from "really-relaxed-json";
import { JSONObject } from "util/jsonTypes";
import {
  ColorMapContext,
  DEFAULT_COLOR_KEY,
  DropdownStyled,
  deriveTagPalette,
  getIconFromOptions,
  getTagColor,
  isStringArray,
  toColorMap,
  WrapperMulti,
} from "./columnTypeUtils/tagUtils";

// Context 用于传递列基础配置的 editable 值
export const ColumnEditableContext = createContext<boolean | undefined>(undefined);

/* ------------------------------ 工具函数区 ------------------------------ */

// 统一解析 value 为字符串数组
export const parseTagsData = (value: unknown): string[] => {
  if (isStringArray(value)) return value.map(String);

  if (["string", "number", "boolean"].includes(typeof value)) {
    if (typeof value === "string") {
      try {
        const result = JSON.parse(toJson(value));
        if (isStringArray(result)) return result.map(String);
      } catch (_) {}
    }
    return [String(value)];
  }

  return [];
};

/* ------------------------------ 控件配置 ------------------------------ */

const TagsArrayControl = codeControl<string[]>(parseTagsData, {
  expectedType: "Array<string>",
  codeType: "JSON",
});

const childrenMap = {
  text: TagsArrayControl,
  colorMap: ColorMapOptionControl,
  allowCustomTags: BoolControl,
  onTagClick: ActionSelectorControl,
};

/* ------------------------------ 数据基础转换 ------------------------------ */

const getBaseValue: ColumnTypeViewFn<
  typeof childrenMap,
  { text: string[]; colorMap?: JSONObject; colorMapOptions?: any[]; allowCustomTags?: boolean },
  { text: string[]; colorMap?: JSONObject; colorMapOptions?: any[]; allowCustomTags?: boolean }
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

type TagsArrayEditProps = {
  value: string[];
  onChange: (value: string[]) => void;
  onChangeEnd: () => void;
  colorMap?: JSONObject;
  allowCustomTags?: boolean;
};

const TagsArrayEdit = ({ value, onChange, onChangeEnd, colorMap, allowCustomTags }: TagsArrayEditProps) => {
  const defaultTags = useContext(TagsContext);
  const contextValue = useContext(ColorMapContext);
  const mergedColorMap = colorMap || contextValue?.colorMap;
  const colorMapOptions = contextValue?.colorMapOptions;
  const allowCustom = allowCustomTags !== false; // 默认允许自定义

  // 从 colorMapOptions 中提取有效的标签 label
  const colorMapLabels = useMemo(() => {
    if (!Array.isArray(colorMapOptions)) return [];
    return colorMapOptions
      .map((opt) => opt?.label)
      .filter((label): label is string => typeof label === "string" && label !== DEFAULT_COLOR_KEY);
  }, [colorMapOptions]);

  // 生成所有可选标签
  const availableTags = useMemo(() => {
    const validDefaults = defaultTags.filter((t): t is string => typeof t === "string");
    const splitDefaults = validDefaults.flatMap((t) => t.split(","));
    return [...new Set([...splitDefaults, ...colorMapLabels])];
  }, [defaultTags, colorMapLabels]);

  const [currentTags, setCurrentTags] = useState(() => parseTagsData(value));
  const [searchValue, setSearchValue] = useState("");

  // 同步外部值变化
  useEffect(() => {
    setCurrentTags(parseTagsData(value));
  }, [value]);

  return (
    <WrapperMulti>
      <Select
        autoFocus
        mode={allowCustom ? "tags" : "multiple"}
        variant="borderless"
        showSearch={allowCustom}
        defaultOpen
        value={currentTags}
        searchValue={searchValue}
        placeholder={trans("table.selectTags")}
        style={{ width: "100%" }}
        onSearch={allowCustom ? setSearchValue : undefined}
        onChange={(vals) => {
          setCurrentTags(vals);
          onChange(vals);
          if (allowCustom) {
            setSearchValue("");
          }
        }}
        onBlur={(e) => {
          if (!e.currentTarget.contains(e.relatedTarget as Node)) onChangeEnd();
        }}
        popupRender={(menu) => (
          <DropdownStyled>
            <ScrollBar style={{ maxHeight: 256 }}>{menu}</ScrollBar>
          </DropdownStyled>
        )}
        tagRender={(props) => {
          const { label, closable, onClose } = props;
          const labelStr = String(label);
          const color = getTagColor(labelStr, mergedColorMap);
          const palette = deriveTagPalette(color);
          const icon = getIconFromOptions(labelStr, colorMapOptions);
          const tagStyle = palette
            ? {
                borderColor: palette.borderColor,
                backgroundColor: palette.backgroundColor,
                color: palette.textColor,
              }
            : { color };
          return (
            <Tag
              color={palette ? undefined : color}
              style={tagStyle}
              icon={icon}
              closable={closable}
              onMouseDown={(e) => {
                e.preventDefault();
                e.stopPropagation();
              }}
              onClose={(e) => {
                e.stopPropagation();
                onClose?.(e);
              }}
            >
              {labelStr}
            </Tag>
          );
        }}
      >
        {availableTags.map((tag) => (
          <Select.Option key={tag} value={tag}>
            {tag}
          </Select.Option>
        ))}
      </Select>
    </WrapperMulti>
  );
};

/* ------------------------------ 列组件构建 ------------------------------ */

export const ColumnTagsComp = new ColumnTypeCompBuilder(
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

    const tags = parseTagsData(value.text);

    return tags.map((tag, i) => {
      const tagColor = getTagColor(tag, latestColorMap);
      const palette = deriveTagPalette(tagColor);
      const tagStyle = palette
        ? {
            borderColor: palette.borderColor,
            backgroundColor: palette.backgroundColor,
            color: palette.textColor,
            cursor: "pointer",
          }
        : { cursor: "pointer", color: tagColor };
      const icon = getIconFromOptions(tag, latestColorMapOptions);
      return (
        <Tag
          color={palette ? undefined : tagColor}
          style={tagStyle}
          icon={icon}
          key={i}
          onClick={(e) => {
            e.stopPropagation();
            dispatch(updateActionContextAction({ clickedTag: tag }));
            props.onTagClick({ clickedTag: tag });
          }}
        >
          {tag}
        </Tag>
      );
    });
  },
  (nodeValue) => parseTagsData(nodeValue.text.value),
  getBaseValue
)
  .setEditViewFn((props) => {
    if (!props.value || typeof props.value !== "object") return null;
    const value = props.value as {
      text: string[];
      colorMap?: JSONObject;
      colorMapOptions?: any[];
      allowCustomTags?: boolean;
    };
    return (
      <ColorMapContext.Provider value={{ colorMap: value.colorMap, colorMapOptions: value.colorMapOptions }}>
        <TagsArrayEdit
          value={value.text}
          colorMap={value.colorMap}
          allowCustomTags={value.allowCustomTags}
          onChange={(text) =>
            props.onChange({
              text,
              colorMap: value.colorMap,
              colorMapOptions: value.colorMapOptions,
              allowCustomTags: value.allowCustomTags,
            })
          }
          onChangeEnd={props.onChangeEnd}
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
        {children.onTagClick.propertyView({
          label: trans("table.onTagClick"),
        })}
      </>
    );
  })
  .build();
