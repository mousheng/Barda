/**
 * Tag和Tags列组件的共享工具函数和常量
 */

import { PresetStatusColorTypes } from "antd/lib/_util/colors";
import _ from "lodash";
import React, { createContext } from "react";
import { toJson } from "really-relaxed-json";
import { JSONObject } from "util/jsonTypes";
import { hashToNum } from "util/stringUtils";
import styled from "styled-components";

/* ------------------------------ 类型定义 ------------------------------ */

export type ColorMapContextValue = {
  colorMap?: JSONObject;
  colorMapOptions?: any[];
};

/* ------------------------------ Context ------------------------------ */

export const ColorMapContext = createContext<ColorMapContextValue | undefined>(undefined);

/* ------------------------------ 常量 ------------------------------ */

export const DEFAULT_COLOR_KEY = "__default__"; // 默认颜色键
const colors = PresetStatusColorTypes;

/* ------------------------------ 工具函数 ------------------------------ */

/**
 * 判断是否为字符串/数字/布尔值数组
 */
export const isStringArray = (val: unknown): val is (string | number | boolean)[] =>
  _.isArray(val) && val.every(v => ["string", "number", "boolean"].includes(typeof v));

/**
 * 将 ColorMapOptionControl 数组转为 {label: color} 映射
 */
export const toColorMap = (options?: any[]): JSONObject => {
  if (!Array.isArray(options)) return {};
  return options.reduce((acc, cur) => {
    if (cur?.label && cur?.color) acc[cur.label] = cur.color;
    return acc;
  }, {} as JSONObject);
};

/**
 * 从 colorMapOptions 中获取指定 label 的 icon
 */
export const getIconFromOptions = (label: string, options?: any[]): React.ReactNode | undefined => {
  if (!Array.isArray(options)) return undefined;
  const option = options.find(opt => opt?.label === label);
  return option?.icon;
};

/**
 * 根据文本获取标签颜色
 * 优先级：colorMap[text] > colorMap[__default__] > 哈希计算颜色
 */
export const getTagColor = (text: string, colorMap?: JSONObject): string => {
  if (colorMap?.[text]) return colorMap[text] as string;
  if (colorMap?.[DEFAULT_COLOR_KEY]) return colorMap[DEFAULT_COLOR_KEY] as string;
  return colors[Math.abs(hashToNum(text)) % colors.length];
};

/**
 * 基于给定颜色生成 Tag 的边框/背景/文字配色
 * 规则：
 *  - 边框：使用原色
 *  - 背景：原色与白色进行混合
 *  - 文字：
 *      · 若输入色过浅 → 使用深色（#333）
 *      · 若背景与边框对比不足 → 使用深色（#333）
 *      · 否则 → 使用原色（边框色）
 */
export const deriveTagPalette = (
  color: string,
  mixRatio: number = 0.75
): { borderColor: string; backgroundColor: string; textColor: string } | null => {
  const hexMatch = /^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/.exec(color);
  if (!hexMatch) return null;

  const hex = hexMatch[1].slice(0, 6);
  const toChannel = (start: number) => parseInt(hex.slice(start, start + 2), 16);
  const [r, g, b] = [toChannel(0), toChannel(2), toChannel(4)];

  const mix = (v: number) => Math.round(v + (255 - v) * mixRatio);
  const toHex = (v: number) => v.toString(16).padStart(2, "0");

  // 背景色
  const bgR = mix(r);
  const bgG = mix(g);
  const bgB = mix(b);
  const backgroundColor = `#${toHex(bgR)}${toHex(bgG)}${toHex(bgB)}`;

  // 相对亮度
  const luminance = (r: number, g: number, b: number) => {
    const ck = (v: number) => {
      v /= 255;
      return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    };
    return 0.2126 * ck(r) + 0.7152 * ck(g) + 0.0722 * ck(b);
  };

  const lumColor = luminance(r, g, b);

  const textColor = lumColor > 0.7 ? "#333333" : color;

  return {
    borderColor: color,
    backgroundColor,
    textColor
  };
};

/**
 * 尝试解析JSON字符串为数组
 */
export const tryParseJSON = (value: string): any => {
  try {
    return JSON.parse(toJson(value));
  } catch (_) {
    return null;
  }
};

/* ------------------------------ 共享样式 ------------------------------ */

/**
 * 编辑框包装器样式（Tag单选专用）
 */
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

/**
 * 编辑框包装器样式（Tags多选专用）
 * 支持多个标签换行并垂直居中对齐
 */
export const WrapperMulti = styled.div`
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
      display: flex;
      flex-wrap: wrap;
      align-content: center;

      .ant-select-selection-item {
        display: inline-flex;
        align-items: center;
        padding-right: 24px;
      }
    }

    .ant-select-selection-search {
      left: 7px;
      input {
        height: 100%;
      }
    }

    &.ant-select-open {
      .ant-select-arrow {
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

/**
 * 下拉列表样式
 */
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

