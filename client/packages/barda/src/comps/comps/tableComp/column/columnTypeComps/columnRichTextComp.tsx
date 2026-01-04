import { CustomModal } from "barda-design";
import { ColumnTypeCompBuilder, ColumnTypeViewFn } from "comps/comps/tableComp/column/columnTypeCompBuilder";
import { ColumnValueTooltip } from "comps/comps/tableComp/column/simpleColumnTypeComps";
import { StringControl } from "comps/controls/codeControl";
import { trans } from "i18n";
import React, { Suspense, useEffect, useRef, useState } from "react";
import type ReactQuill from "react-quill-new";
import styled, { css } from "styled-components";
import { INPUT_DEFAULT_ONCHANGE_DEBOUNCE } from "constants/perf";
import _ from "lodash";
import { Skeleton } from "antd";

const ReactQuillEditor = React.lazy(() => import("react-quill-new"));

const localizeStyle = css`
  & .ql-snow {
    .ql-picker.ql-header {
      .ql-picker-label::before,
      .ql-picker-item::before {
        content: "${trans("richTextEditor.content")}";
      }

      .ql-picker-label[data-value="1"]::before,
      .ql-picker-item[data-value="1"]::before {
        content: "${trans("richTextEditor.title")} 1";
      }

      .ql-picker-label[data-value="2"]::before,
      .ql-picker-item[data-value="2"]::before {
        content: "${trans("richTextEditor.title")} 2";
      }

      .ql-picker-label[data-value="3"]::before,
      .ql-picker-item[data-value="3"]::before {
        content: "${trans("richTextEditor.title")} 3";
      }
    }
    & .ql-tooltip.ql-editing a.ql-action::after {
      content: "${trans("richTextEditor.save")}";
    }
    & .ql-tooltip::before {
      content: "${trans("richTextEditor.link")}";
    }
    & .ql-tooltip {
      a.ql-action::after {
        content: "${trans("richTextEditor.edit")}";
      }
      a.ql-remove::before {
        content: "${trans("richTextEditor.remove")}";
      }
    }
  }
`;

const ModalEditorWrapper = styled.div`
  ${localizeStyle}
  width: 100%;
  min-height: 400px;

  & .ql-editor {
    min-height: 350px;
  }
`;

const DisplayWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  
  .status-text {
    font-size: 12px;
    color: #666;
    white-space: nowrap;
    position: relative;
    padding-left: 18px;
    
    &::before {
      content: "✓";
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      font-size: 14px;
      line-height: 1;
    }
    
    &.all-completed::before {
      color: #52c41a;
    }
    
    &.has-uncompleted::before {
      color: #999;
    }
  }
  
  .content-preview {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #333;
  }
`;

/**
 * 统计富文本内容中 checklist 的数量
 * @param htmlContent HTML 内容字符串
 * @returns 包含已完成和未完成数量的对象
 */
function countChecklistItems(htmlContent: string): { completed: number; uncompleted: number } {
  if (!htmlContent) {
    return { completed: 0, uncompleted: 0 };
  }

  // 使用正则表达式匹配 Quill checklist 的 HTML 结构
  const checkedPattern = /<li[^>]*data-list\s*=\s*["']?checked["']?[^>]*>|<li[^>]*class\s*=\s*["']?[^"'>]*ql-list-checked[^"'>]*["']?[^>]*>/gi;
  const uncheckedPattern = /<li[^>]*data-list\s*=\s*["']?unchecked["']?[^>]*>|<li[^>]*class\s*=\s*["']?[^"'>]*ql-list-unchecked[^"'>]*["']?[^>]*>/gi;

  const completedMatches = htmlContent.match(checkedPattern);
  const uncompletedMatches = htmlContent.match(uncheckedPattern);

  return {
    completed: completedMatches ? completedMatches.length : 0,
    uncompleted: uncompletedMatches ? uncompletedMatches.length : 0,
  };
}

/**
 * 从 HTML 中提取纯文本（去除 HTML 标签，排除 checklist 项）
 */
function extractTextFromHtml(html: string): string {
  if (!html) return "";
  // 创建一个临时 div 来解析 HTML
  const tempDiv = document.createElement("div");
  tempDiv.innerHTML = html;
  
  // 移除所有 checklist 项（data-list="checked" 或 data-list="unchecked" 的 li 元素）
  const checklistItems = tempDiv.querySelectorAll('li[data-list="checked"], li[data-list="unchecked"]');
  checklistItems.forEach(item => item.remove());
  
  // 也移除通过 class 标识的 checklist 项
  const checklistItemsByClass = tempDiv.querySelectorAll('li.ql-list-checked, li.ql-list-unchecked');
  checklistItemsByClass.forEach(item => item.remove());
  
  return tempDiv.textContent || tempDiv.innerText || "";
}

const childrenMap = {
  text: StringControl,
};

const getBaseValue: ColumnTypeViewFn<typeof childrenMap, string, string> = (props) => props.text;

/**
 * 富文本编辑器模态框组件
 */
function RichTextModalEditor(props: {
  value: string;
  onChange: (value: string) => void;
  onCancel: () => void;
  onOk: () => void;
  editorRef?: React.MutableRefObject<ReactQuill | null>;
}) {
  const [content, setContent] = useState("");
  const internalEditorRef = useRef<ReactQuill>(null);
  const editorRef = props.editorRef || internalEditorRef;
  const debounce = INPUT_DEFAULT_ONCHANGE_DEBOUNCE;

  const originOnChangeRef = useRef(props.onChange);
  originOnChangeRef.current = props.onChange;

  const onChangeRef = useRef(
    debounce > 0
      ? _.debounce((v: string) => {
          originOnChangeRef.current?.(v);
        })
      : (v: string) => originOnChangeRef.current?.(v)
  );

  const handleChange = (value: string) => {
    setContent(value);
    onChangeRef.current(value);
  };

  useEffect(() => {
    let finalValue = props.value;
    if (!/^<\w+>.+<\/\w+>$/.test(props.value)) {
      finalValue = `<p class="">${props.value}</p>`;
    }
    setContent(finalValue);
  }, [props.value]);

  const toolbarOptions = [
    [{ header: [1, 2, 3, false] }],
    ["bold", "italic", "underline", "strike", "blockquote"],
    [{ list: "ordered" }, { list: "bullet" }, { list: "check" }],
    [{ indent: "-1" }, { indent: "+1" }],
    [{ color: [] }, { background: [] }, { align: [] }],
    ["link", "image"],
    ["clean"],
  ];

  return (
    <ModalEditorWrapper>
      <Suspense fallback={<Skeleton />}>
        <ReactQuillEditor
          ref={editorRef}
          modules={{
            toolbar: toolbarOptions,
          }}
          theme="snow"
          value={content}
          onChange={handleChange}
        />
      </Suspense>
    </ModalEditorWrapper>
  );
}

/**
 * 富文本显示组件
 */
function RichTextDisplay(props: { value: string }) {
  const { completed, uncompleted } = countChecklistItems(props.value);
  const textContent = extractTextFromHtml(props.value);
  const total = completed + uncompleted;
  
  let statusText = "";
  let statusClass = "";
  if (total > 0) {
    statusText = `${completed}/${total}`;
    statusClass = uncompleted === 0 ? "all-completed" : "has-uncompleted";
  }

  // 如果没有内容，不显示任何内容（不显示placeholder）
  if (!textContent && !statusText) {
    return null;
  }

  return (
    <DisplayWrapper>
      {statusText && <span className={`status-text ${statusClass}`}>{statusText}</span>}
      {textContent && <span className="content-preview">{textContent}</span>}
    </DisplayWrapper>
  );
}

/**
 * 富文本编辑模态框组件（用于编辑视图）
 */
function RichTextEditModal(props: {
  value: string;
  onChange: (value: string) => void;
  onChangeEnd: () => void;
}) {
  const [isModalOpen, setIsModalOpen] = useState(true);
  const [currentValue, setCurrentValue] = useState(props.value || "");
  const editorRef = useRef<ReactQuill>(null);

  useEffect(() => {
    setCurrentValue(props.value || "");
  }, [props.value]);

  // 关闭模态框时自动提交修改
  const handleCancel = () => {
    // 从编辑器直接获取最新值
    const editor = (editorRef.current as any)?.getEditor?.();
    const finalValue = editor ? editor.root.innerHTML : currentValue;
    props.onChange(finalValue);
    setTimeout(() => {
      props.onChangeEnd();
    }, 100);
    setIsModalOpen(false);
  };

  return (
    <CustomModal
      open={isModalOpen}
      onCancel={handleCancel}
      width="800px"
      showOkButton={false}
      showCancelButton={false}
      afterOpenChange={()=>{
        editorRef?.current?.focus();
      }}
    >
      <RichTextModalEditor
        value={currentValue}
        onChange={props.onChange}
        onCancel={handleCancel}
        onOk={handleCancel}
        editorRef={editorRef}
      />
    </CustomModal>
  );
}

export const ColumnRichTextComp = (function () {
  return new ColumnTypeCompBuilder(
    childrenMap,
    (props, dispatch) => {
      const value = props.changeValue ?? getBaseValue(props, dispatch);
      // 显示视图：只显示状态和文本预览
      return <RichTextDisplay value={value} />;
    },
    (nodeValue) => nodeValue.text.value,
    getBaseValue
  )
    .setEditViewFn((props) => {
      return <RichTextEditModal value={props.value} onChange={props.onChange} onChangeEnd={props.onChangeEnd} />;
    })
    .setPropertyViewFn((children) => (
      <>
        {children.text.propertyView({
          label: trans("table.columnValue"),
          tooltip: ColumnValueTooltip,
        })}
      </>
    ))
    .build();
})();

