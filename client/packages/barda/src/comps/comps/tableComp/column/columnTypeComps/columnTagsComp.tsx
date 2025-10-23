import { Select, Tag } from "antd";
import { PresetStatusColorTypes } from "antd/lib/_util/colors";
import { ScrollBar } from "barda-design";
import { TagsContext } from "components/table/EditableCell";
import { ColumnTypeCompBuilder, ColumnTypeViewFn } from "comps/comps/tableComp/column/columnTypeCompBuilder";
import { ColumnValueTooltip } from "comps/comps/tableComp/column/simpleColumnTypeComps";
import { codeControl, jsonObjectControl } from "comps/controls/codeControl";
import { trans } from "i18n";
import _ from "lodash";
import React, { createContext, useContext, useEffect, useState } from "react";
import { toJson } from "really-relaxed-json";
import styled from "styled-components";
import { JSONObject } from "util/jsonTypes";
import { hashToNum } from "util/stringUtils";

export const ColorMapContext = createContext<JSONObject | undefined>(undefined);

const colors = PresetStatusColorTypes;

// 检查是否为字符串数组
const isStringArray = (value: unknown): value is (string | number | boolean)[] => {
  return (
    _.isArray(value) &&
    value.every((v) => {
      const type = typeof v;
      return type === "string" || type === "number" || type === "boolean";
    })
  );
};

// 解析标签数据为字符串数组
const parseTagsData = (value: unknown): string[] => {
  if (isStringArray(value)) {
    return (value as (string | number | boolean)[]).map((text) => String(text));
  }
  if (typeof value === "string") {
    try {
      const result = JSON.parse(toJson(value));
      if (isStringArray(result)) {
        return (result as (string | number | boolean)[]).map((text) => String(text));
      }
      return [value];
    } catch (e) {
      return [value];
    }
  }
  if (typeof value === "number" || typeof value === "boolean") {
    return [String(value)];
  }
  return [];
};

// 字符串数组输入控制
const TagsArrayControl = codeControl<string[]>(
  (value: unknown) => {
    if (isStringArray(value)) {
      return (value as (string | number | boolean)[]).map((text) => String(text));
    }
    const valueType = typeof value;
    if (valueType === "string") {
      try {
        const result = JSON.parse(toJson(value));
        if (isStringArray(result)) {
          return (result as (string | number | boolean)[]).map((text) => String(text));
        }
        return [String(value)];
      } catch (e) {
        return [String(value)];
      }
    } else if (valueType === "number" || valueType === "boolean") {
      return [String(value)];
    }
    throw new TypeError(`Type "Array<string>" is required, but find value: ${JSON.stringify(value)}`);
  },
  { expectedType: "Array<string>", codeType: "JSON" }
);

// 颜色映射控制（JSON对象）
const ColorMapControl = jsonObjectControl({});

// 根据文本和颜色映射获取标签颜色
function getTagColor(text: string, colorMap?: JSONObject): string {
  // 如果有颜色映射，先检查是否有给定key的颜色
  if (colorMap && typeof colorMap[text] === "string") {
    return colorMap[text] as string;
  }

  // 检查是否有default key
  if (colorMap && typeof colorMap["default"] === "string") {
    return colorMap["default"] as string;
  }

  // 使用PresetStatusColorTypes计算颜色
  const index = Math.abs(hashToNum(text)) % colors.length;
  return colors[index];
}

const childrenMap = {
  text: TagsArrayControl,
  colorMap: ColorMapControl,
};

const getBaseValue: ColumnTypeViewFn<
  typeof childrenMap,
  { text: string[]; colorMap?: JSONObject },
  { text: string[]; colorMap?: JSONObject }
> = (props) => ({ text: props.text, colorMap: props.colorMap });

type TagsArrayEditPropsType = {
  value: string[];
  onChange: (value: string[]) => void;
  onChangeEnd: () => void;
  colorMap?: JSONObject;
};

export const Wrapper = styled.div`
  display: inline-flex;
  align-items: center;
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  background: transparent !important;
  > div {
    width: 100%;
    height: 100%;
  }
  .ant-select {
    height: 100%;
    .ant-select-selector {
      padding: 0 7px;
      height: 100%;
      overflow: hidden;
      flex-wrap: wrap;
      display: flex;
      align-content: center;
      .ant-select-selection-item {
        display: inline-flex;
        align-items: center;
        padding-right: 24px;
      }
    }
    .ant-select-selector .ant-select-selection-search {
      left: 7px;
      input {
        height: 100%;
      }
    }
    &.ant-select-open {
      .ant-select-arrow {
        border-right: none;
        border-left: 1px solid #d7d9e0;
        svg g path {
          fill: #315efb;
        }
      }
      .ant-select-selection-item {
        opacity: 0.4;
      }
    }
  }
`;

export const DropdownStyled = styled.div`
  .ant-select-item {
    padding: 4px 12px;
  }
`;

const TagsArrayEdit = (props: TagsArrayEditPropsType) => {
  const defaultTags = useContext(TagsContext);
  const colorMapFromContext = useContext(ColorMapContext);
  const colorMap = props.colorMap || colorMapFromContext;

  const [availableTags] = useState<string[]>(() =>
    defaultTags.flatMap((item) => (item.includes(",") ? item.split(",") : [item]))
  );

  const [searchValue, setSearchValue] = useState("");
  const [currentTags, setCurrentTags] = useState<string[]>(() => parseTagsData(props.value));

  // 同步外部值变化
  useEffect(() => {
    setCurrentTags(parseTagsData(props.value));
  }, [props.value]);

  return (
    <Wrapper>
      <Select
        autoFocus
        mode="tags"
        variant="borderless"
        showSearch
        defaultOpen
        value={currentTags}
        style={{ width: "100%" }}
        placeholder={trans("table.selectTags")}
        searchValue={searchValue}
        onSearch={setSearchValue}
        onChange={(selectedTexts) => {
          setCurrentTags(selectedTexts);
          props.onChange(selectedTexts);
          setSearchValue("");
        }}
        onBlur={(e) => {
          // 点击Select外部才触发onChangeEnd
          if (!e.currentTarget.contains(e.relatedTarget as Node)) {
            props.onChangeEnd();
          }
        }}
        dropdownRender={(originNode) => (
          <DropdownStyled>
            <ScrollBar style={{ maxHeight: "256px" }}>{originNode}</ScrollBar>
          </DropdownStyled>
        )}
        tagRender={(tagProps) => {
          const { label, closable, onClose } = tagProps;
          const labelStr = String(label || "");
          const color = getTagColor(labelStr, colorMap);
          const onPreventMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
            e.preventDefault();
            e.stopPropagation();
          };
          return (
            <Tag
              color={color}
              closable={closable}
              onMouseDown={onPreventMouseDown}
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
        {availableTags.map((tag, index) => (
          <Select.Option value={tag} key={index}>
            {tag}
          </Select.Option>
        ))}
      </Select>
    </Wrapper>
  );
};

export const ColumnTagsComp = (function () {
  return new ColumnTypeCompBuilder(
    childrenMap,
    (props, dispatch) => {
      const value = props.changeValue ?? getBaseValue(props, dispatch);
      const tags = parseTagsData(value.text);
      const colorMap = value.colorMap;
      const view = tags.map((tag, index) => {
        return (
          <Tag color={getTagColor(tag, colorMap)} key={index}>
            {tag}
          </Tag>
        );
      });
      return view;
    },
    (nodeValue) => {
      const text = nodeValue.text.value;
      const tags = parseTagsData(text);
      return tags;
    },
    getBaseValue
  )
    .setEditViewFn((props) => {
      return (
        <ColorMapContext.Provider value={props.value.colorMap}>
          <TagsArrayEdit
            value={props.value.text}
            onChange={(newText) => props.onChange({ text: newText, colorMap: props.value.colorMap })}
            onChangeEnd={props.onChangeEnd}
          />
        </ColorMapContext.Provider>
      );
    })
    .setPropertyViewFn((children) => (
      <>
        {children.text.propertyView({
          label: trans("table.columnValue"),
          tooltip: ColumnValueTooltip,
        })}
        {children.colorMap.propertyView({
          label: trans("table.colorMap") as any,
          tooltip: trans("table.colorMapTooltip") as any,
        })}
      </>
    ))
    .build();
})();
