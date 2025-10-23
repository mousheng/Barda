import { Tag } from "antd";
import { PresetStatusColorTypes } from "antd/lib/_util/colors";
import { TagsContext } from "components/table/EditableCell";
import {
  ColumnTypeCompBuilder,
  ColumnTypeViewFn,
} from "comps/comps/tableComp/column/columnTypeCompBuilder";
import { ColumnValueTooltip } from "comps/comps/tableComp/column/simpleColumnTypeComps";
import { codeControl, jsonObjectControl } from "comps/controls/codeControl";
import { trans } from "i18n";
import styled from "styled-components";
import _ from "lodash";
import { ReactNode, useContext, useState, createContext } from "react";
import { toJson } from "really-relaxed-json";
import { hashToNum } from "util/stringUtils";
import { CustomSelect, PackUpIcon } from "barda-design";
import { ScrollBar } from "barda-design";
import { JSONObject } from "util/jsonTypes";

export const ColorMapContext = createContext<JSONObject | undefined>(undefined);

const colors = PresetStatusColorTypes;

const isStringArray = (value: any) => {
  return (
    _.isArray(value) &&
    value.every((v) => {
      const type = typeof v;
      return type === "string" || type === "number" || type === "boolean";
    })
  );
};

// accept string, number, boolean and array input
const TagsControl = codeControl<Array<string> | string>(
  (value) => {
    if (isStringArray(value)) {
      return value;
    }
    const valueType = typeof value;
    if (valueType === "string") {
      try {
        const result = JSON.parse(toJson(value));
        if (isStringArray(result)) {
          return result;
        }
        return value;
      } catch (e) {
        return value;
      }
    } else if (valueType === "number" || valueType === "boolean") {
      return value;
    }
    throw new TypeError(
      `Type "Array<string> | string" is required, but find value: ${JSON.stringify(value)}`
    );
  },
  { expectedType: "string | Array<string>", codeType: "JSON" }
);

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
  text: TagsControl,
  colorMap: jsonObjectControl({}),
};

const getBaseValue: ColumnTypeViewFn<typeof childrenMap, { text: string | string[]; colorMap?: JSONObject }, { text: string | string[]; colorMap?: JSONObject }> = (
  props
) => ({ text: props.text, colorMap: props.colorMap });

type TagEditPropsType = {
  value: string | string[];
  onChange: (value: string | string[]) => void;
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
    padding: 3px 8px;
    margin: 0 0 2px 8px;
    border-radius: 4px;
  }
  .ant-select-item-option-content {
    display: flex;
    align-items: center;
  }
  .ant-tag {
    margin-right: 0;
  }
`;

const TagEdit = (props: TagEditPropsType) => {
  const defaultTags = useContext(TagsContext);
  const colorMapFromContext = useContext(ColorMapContext);
  const colorMap = props.colorMap || colorMapFromContext;
  
  const [tags, setTags] = useState(() => {
    const result: string[] = [];
    defaultTags.forEach((item) => {
      if (item.split(",")[1]) {
        item.split(",").forEach((tag) => result.push(tag));
      }
      result.push(item);
    });
    return result;
  });
  return (
    <Wrapper>
      <CustomSelect
        autoFocus
        defaultOpen
        variant="borderless"
        optionLabelProp="children"
        showSearch
        defaultValue={props.value}
        style={{ width: "100%" }}
        suffixIcon={<PackUpIcon />}
        onSearch={(value) => {
          if (defaultTags.findIndex((item) => item.includes(value)) < 0) {
            setTags([...defaultTags, value]);
          } else {
            setTags(defaultTags);
          }
          props.onChange(value);
        }}
        onChange={(value) => {
          props.onChange(value);
        }}
        dropdownRender={(originNode: ReactNode) => (
          <DropdownStyled>
            <ScrollBar style={{ maxHeight: "256px" }}>{originNode}</ScrollBar>
          </DropdownStyled>
        )}
        dropdownStyle={{ marginTop: "7px", padding: "8px 0 6px 0" }}
        onBlur={props.onChangeEnd}
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            props.onChangeEnd();
          }
        }}
      >
        {tags.map((value, index) => (
          <CustomSelect.Option value={value} key={index}>
            {value.split(",")[1] ? (
              value.split(",").map((item, i) => (
                <Tag color={getTagColor(item, colorMap)} key={i} style={{ marginRight: "8px" }}>
                  {item}
                </Tag>
              ))
            ) : (
              <Tag color={getTagColor(value, colorMap)} key={index}>
                {value}
              </Tag>
            )}
          </CustomSelect.Option>
        ))}
      </CustomSelect>
    </Wrapper>
  );
};

export const ColumnTagComp = (function () {
  return new ColumnTypeCompBuilder(
    childrenMap,
    (props, dispatch) => {
      const baseValue = props.changeValue ?? getBaseValue(props, dispatch);
      let value: string | string[] = baseValue.text;
      value = typeof value === "string" && value.split(",")[1] ? value.split(",") : value;
      const tags = _.isArray(value) ? value : [value];
      const colorMap = baseValue.colorMap;
      const view = tags.map((tag, index) => {
        // The actual eval value is of type number or boolean
        const tagText = String(tag);
        return (
          <Tag color={getTagColor(tagText, colorMap)} key={index}>
            {tagText}
          </Tag>
        );
      });
      return view;
    },
    (nodeValue) => {
      const text = nodeValue.text.value;
      return _.isArray(text) ? text.join(",") : text;
    },
    getBaseValue
  )
    .setEditViewFn((props) => {
      const text = props.value.text;
      const value = _.isArray(text) ? text.join(",") : text;
      return (
        <ColorMapContext.Provider value={props.value.colorMap}>
          <TagEdit 
            value={value} 
            onChange={(newValue: string | string[]) => props.onChange({ text: newValue, colorMap: props.value.colorMap })} 
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
