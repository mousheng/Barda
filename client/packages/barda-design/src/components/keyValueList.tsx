import { PlusOutlined, UnorderedListOutlined } from "@ant-design/icons";
import { Button, Input } from "antd";
import { trans } from "i18n/design";
import { ReactComponent as Bin } from "icons/icon-recycle-bin.svg";
import { ReactNode, useState } from "react";
import styled from "styled-components";
import { CustomModal } from "./CustomModal";

const KeyValueListItem = styled.div`
  display: flex;
  width: 100%;
  align-items: center;
  margin-bottom: 8px;
`;

const DelIcon = styled(Bin)<{
  $forbidden?: boolean;
}>`
  &&& {
    height: 16px;
    width: 16px;
    margin-left: 8px;
    flex-shrink: 0;

    g g {
      ${(props) => props.$forbidden && "stroke: #D7D9E0;"}
    }

    &:hover {
      cursor: ${(props) => (props.$forbidden ? "default" : "pointer")};
    }

    &:hover g {
      ${(props) => !props.$forbidden && "stroke: #315efb;"}
    }
  }
`;

const TextAreaWrapper = styled.div`
  margin: 16px 0;

  textarea {
    min-height: 200px;
    font-family: monospace;
    font-size: 12px;
  }
`;

/**
 * 解析HTTP头部格式的文本
 * 格式: Key: Value
 * 例如: Accept: application/json, text/plain, *\/*
 */
const parseHttpHeaders = (text: string): Array<{ key: string; value: string }> => {
  const lines = text.split("\n");
  const result: Array<{ key: string; value: string }> = [];

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) continue;

    const colonIndex = trimmed.indexOf(":");
    if (colonIndex === -1) continue;

    const key = trimmed.substring(0, colonIndex).trim();
    const value = trimmed.substring(colonIndex + 1).trim();

    if (key) {
      result.push({ key, value });
    }
  }

  return result;
};

/**
 * 将 key-value 对数组格式化为 HTTP 头部格式的文本
 */
const formatHttpHeaders = (items: Array<{ key: string; value: string }>): string => {
  return items.map((item) => `${item.key}: ${item.value}`).join("\n");
};

/**
 * 解析JSON格式的文本
 * 格式: { "key1": "value1", "key2": "value2" }
 */
const parseJson = (text: string): Array<{ key: string; value: string }> => {
  try {
    const trimmed = text.trim();
    if (!trimmed) return [];

    const parsed = JSON.parse(trimmed);
    if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
      return [];
    }

    return Object.entries(parsed).map(([key, value]) => ({
      key: String(key),
      value: String(value ?? ""),
    }));
  } catch (error) {
    return [];
  }
};

/**
 * 将 key-value 对数组格式化为 JSON 格式的文本
 */
const formatJson = (items: Array<{ key: string; value: string }>): string => {
  const obj: Record<string, string> = {};
  items.forEach((item) => {
    if (item.key) {
      obj[item.key] = item.value;
    }
  });
  return JSON.stringify(obj, null, 2);
};

/**
 * 解析URL参数格式的文本
 * 格式: key1=value1&key2=value2
 * 例如: name=John&age=30
 */
const parseUrlParams = (text: string): Array<{ key: string; value: string }> => {
  const trimmed = text.trim();
  if (!trimmed) return [];

  const result: Array<{ key: string; value: string }> = [];
  const pairs = trimmed.split("&");

  for (const pair of pairs) {
    const trimmedPair = pair.trim();
    if (!trimmedPair) continue;

    const equalIndex = trimmedPair.indexOf("=");
    if (equalIndex === -1) {
      // 如果没有等号，将整个字符串作为 key，value 为空
      result.push({ key: trimmedPair, value: "" });
    } else {
      const key = trimmedPair.substring(0, equalIndex).trim();
      const value = trimmedPair.substring(equalIndex + 1).trim();
      if (key) {
        result.push({ key, value });
      }
    }
  }

  return result;
};

/**
 * 将 key-value 对数组格式化为 URL 参数格式的文本
 */
const formatUrlParams = (items: Array<{ key: string; value: string }>): string => {
  return items
    .filter((item) => item.key) // 只包含有 key 的项
    .map((item) => `${item.key}=${item.value}`)
    .join("&");
};

export const KeyValueList = (props: {
  list: ReactNode[];
  onAdd: () => void;
  onDelete: (item: ReactNode, index: number) => void;
  onBatchAdd?: (items: Array<{ key: string; value: string }>) => void;
  getKeyValuePairs?: () => Array<{ key: string; value: string }>;
  type?: "headers" | "json" | "url";
  jsonEditorComponent?: React.ComponentType<{ value: string; onChange: (value: string) => void; placeholder?: string }>;
}) => {
  const [batchAddVisible, setBatchAddVisible] = useState(false);
  const [batchAddText, setBatchAddText] = useState("");
  const formatType = props.type || "headers";
  const JsonEditor = props.jsonEditorComponent;

  const handleOpenBatchAdd = () => {
    // 打开模态框时，将现有的 key-value 对提取到输入框
    if (props.getKeyValuePairs) {
      const currentPairs = props.getKeyValuePairs();
      let formattedText = "";
      if (formatType === "json") {
        formattedText = formatJson(currentPairs);
      } else if (formatType === "url") {
        formattedText = formatUrlParams(currentPairs);
      } else {
        formattedText = formatHttpHeaders(currentPairs);
      }
      setBatchAddText(formattedText);
    }
    setBatchAddVisible(true);
  };

  const handleBatchAdd = () => {
    let parsed: Array<{ key: string; value: string }>;
    if (formatType === "json") {
      parsed = parseJson(batchAddText);
    } else if (formatType === "url") {
      parsed = parseUrlParams(batchAddText);
    } else {
      parsed = parseHttpHeaders(batchAddText);
    }
    // 即使解析结果为空，也至少保留一个空的 key-value 对，方便用户输入
    const itemsToAdd = parsed.length > 0 ? parsed : [{ key: "", value: "" }];

    if (props.onBatchAdd) {
      // 使用 onBatchAdd 替换整个列表
      props.onBatchAdd(itemsToAdd);
    } else {
      // 如果没有提供 onBatchAdd，则多次调用 onAdd（向后兼容）
      itemsToAdd.forEach(() => props.onAdd());
    }
    setBatchAddVisible(false);
    setBatchAddText("");
  };

  return (
    <>
      {props.list.map((item, index) => (
        <KeyValueListItem key={index /* FIXME: find a proper key instead of `index` */}>
          {item}
          <DelIcon
            onClick={() => props.list.length > 1 && props.onDelete(item, index)}
            $forbidden={props.list.length === 1}
          />
        </KeyValueListItem>
      ))}
      <div style={{ display: "flex", alignItems: "center" }}>
        <Button
          variant="text"
          onClick={props.onAdd}
          icon={<PlusOutlined />}
          color="primary"
          styles={{ root: { gap: 4 } }}
        >
          {trans("addItem")}
        </Button>
        {props.type && (
          <Button
            variant="text"
            color="primary"
            onClick={handleOpenBatchAdd}
            icon={<UnorderedListOutlined />}
            styles={{ root: { gap: 4 } }}
          >
            {trans("keyValueList.batchAdd")}
          </Button>
        )}
      </div>
      <CustomModal
        open={batchAddVisible}
        title={trans("keyValueList.batchAddTitle")}
        onCancel={() => {
          setBatchAddVisible(false);
          setBatchAddText("");
        }}
        onOk={handleBatchAdd}
        width="600px"
      >
        <TextAreaWrapper>
          {formatType === "json" && JsonEditor ? (
            <JsonEditor
              value={batchAddText}
              onChange={setBatchAddText}
              placeholder='Paste JSON object here. Example:\n{\n  "key1": "value1",\n  "key2": "value2"\n}'
            />
          ) : (
            <Input.TextArea
              value={batchAddText}
              onChange={(e) => setBatchAddText(e.target.value)}
              placeholder={
                formatType === "url"
                  ? "Paste URL parameters here. Example:\nkey1=value1&key2=value2"
                  : trans("keyValueList.batchAddPlaceholder")
              }
              rows={10}
              spellCheck={false}
            />
          )}
        </TextAreaWrapper>
      </CustomModal>
    </>
  );
};
