import { StringControl } from "comps/controls/codeControl";
import { BoolControl } from "comps/controls/boolControl";
import { stringExposingStateControl } from "comps/controls/codeStateControl";
import { AutoHeightControl } from "comps/controls/autoHeightControl";
import { ChangeEventHandlerControl } from "comps/controls/eventHandlerControl";
import { UICompBuilder, withDefault } from "comps/generators";
import { NameConfig, NameConfigHidden, withExposingConfigs, depsConfig } from "comps/generators/withExposing";
import { Section, sectionNames } from "barda-design";
import React, { Suspense, useEffect, useRef, useState } from "react";
import type ReactQuill from "react-quill-new";
import { Quill } from "react-quill-new";
import styled, { css } from "styled-components";
import { formDataChildren, FormDataPropertyView } from "./formComp/formDataConstants";
import { INPUT_DEFAULT_ONCHANGE_DEBOUNCE } from "constants/perf";
import {
  hiddenPropertyView,
  placeholderPropertyView,
  readOnlyPropertyView,
} from "comps/utils/propertyUtils";
import _ from "lodash";
import { trans } from "i18n";
import { Skeleton } from "antd";
import { styleControl } from "comps/controls/styleControl";
import { RichTextEditorStyle, RichTextEditorStyleType } from "comps/controls/styleControlConstants";

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

      .ql-picker-item[data-value="1"]::before {
        font-size: 26px;
      }

      .ql-picker-item[data-value="3"]::before {
        font-size: 19px;
      }

      .ql-picker-item[data-value="3"]::before {
        font-size: 15px;
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

const commonStyle = (style: RichTextEditorStyleType) => css`
  height: 100%;

  & .ql-editor {
    min-height: 85px;
    &.ql-blank:focus::before,
    &:focus::before {
      display: none;
    }
  }
  & .ql-snow .ql-tooltip.ql-editing {
    input[type="text"] {
      width: 130px;
    }
  }
  & .ql-snow {
    &.ql-container,
    &.ql-toolbar {
      border-color: ${style.border};
      background-color: #ffffff;
    }
  }
  & .ql-toolbar {
    border-radius: ${style.radius} ${style.radius} 0 0;
  }
  & .ql-container {
    border-radius: 0 0 ${style.radius} ${style.radius};
  }
`;

const hideToolbarStyle = (style: RichTextEditorStyleType) => css`
  .ql-snow.ql-toolbar {
    height: 0;
    overflow: hidden;
    padding: 0;
    border-bottom: 0;
    border: none;
  }
  .quill .ql-snow.ql-container {
    border-radius: ${style.radius};
    border: 1px solid ${style.border};
  }
`;

interface Props {
  $hideToolbar: boolean;
  $style: RichTextEditorStyleType;
}

const AutoHeightReactQuill = styled.div<Props>`
  ${localizeStyle}
  ${(props) => commonStyle(props.$style)}
  & .ql-container .ql-editor {
    min-height: 125px;
  }
  ${(props) => (props.$hideToolbar ? hideToolbarStyle(props.$style) : "")};
`;

const FixHeightReactQuill = styled.div<Props>`
  ${localizeStyle}
  ${(props) => commonStyle(props.$style)}
  & .quill {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  & .ql-snow {
    &.ql-container {
      flex: 1;
      overflow: auto;
    }
  }
  ${(props) => (props.$hideToolbar ? hideToolbarStyle(props.$style) : "")};
`;

const TimestampEmbed = Quill.import("blots/embed") as any;

class TimestampBlot extends TimestampEmbed {
  static blotName = "timestamp";

  static tagName = "span";

  static className = "ql-timestamp";

  static create(value: string) {
    const node = super.create() as HTMLElement;
    const text = typeof value === "string" ? value : "";
    node.setAttribute("data-timestamp", text);
    node.setAttribute("contenteditable", "false");
    node.innerText = ` (${text})`;
    return node;
  }

  static value(node: HTMLElement) {
    return node.getAttribute("data-timestamp") || "";
  }
}

Quill.register("formats/timestamp", TimestampBlot);

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
  // Quill 的 checklist 在 HTML 中使用 data-list 属性：<li data-list="checked"> 或 <li data-list="unchecked">
  // 同时也支持 class 属性格式（兼容性）：<li class="...ql-list-checked...">
  // 支持单引号、双引号或没有引号的属性值
  const checkedPattern = /<li[^>]*data-list\s*=\s*["']?checked["']?[^>]*>|<li[^>]*class\s*=\s*["']?[^"'>]*ql-list-checked[^"'>]*["']?[^>]*>/gi;
  const uncheckedPattern = /<li[^>]*data-list\s*=\s*["']?unchecked["']?[^>]*>|<li[^>]*class\s*=\s*["']?[^"'>]*ql-list-unchecked[^"'>]*["']?[^>]*>/gi;

  const completedMatches = htmlContent.match(checkedPattern);
  const uncompletedMatches = htmlContent.match(uncheckedPattern);

  return {
    completed: completedMatches ? completedMatches.length : 0,
    uncompleted: uncompletedMatches ? uncompletedMatches.length : 0,
  };
}

const childrenMap = {
  value: stringExposingStateControl("value"),
  hideToolbar: BoolControl,
  readOnly: BoolControl,
  autoHeight: AutoHeightControl,
  autoTimestamp: BoolControl,
  placeholder: withDefault(StringControl, trans("richTextEditor.placeholder")),
  onEvent: ChangeEventHandlerControl,
  style: styleControl(RichTextEditorStyle),

  ...formDataChildren,
};

const toolbarOptions = [
  [{ header: [1, 2, 3, false] }],
  ["bold", "italic", "underline", "strike", "blockquote"],
  [{ list: "ordered" }, { list: "bullet" }, { list: "check" }],
  [{ indent: "-1" }, { indent: "+1" }],
  [{ color: [] }, { background: [] }, { align: [] }],
  ["link", "image"],
  ["clean"],
];

interface IProps {
  value: string;
  placeholder: string;
  hideToolbar: boolean;
  readOnly: boolean;
  autoTimestamp: boolean;
  autoHeight: boolean;
  onChange: (value: string) => void;
  $style: RichTextEditorStyleType;
}

const ReactQuillEditor = React.lazy(() => import("react-quill-new"));

function RichTextEditor(props: IProps) {
  const [content, setContent] = useState("");
  const wrapperRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<ReactQuill>(null);
  const isTypingRef = useRef(0);
  const listenerRef = useRef<{ handler?: (...args: any[]) => void; mouseHandler?: (e: MouseEvent) => void; keyHandler?: (e: KeyboardEvent) => void } | null>(null);
  const listenerBoundRef = useRef(false);
  const isHandlingEnterRef = useRef(false);

  const debounce = INPUT_DEFAULT_ONCHANGE_DEBOUNCE;

  const originOnChangeRef = useRef(props.onChange);
  originOnChangeRef.current = props.onChange;

  const onChangeRef = useRef(
    debounce > 0
      ? _.debounce((v: string) => {
          window.clearTimeout(isTypingRef.current);
          isTypingRef.current = window.setTimeout(() => (isTypingRef.current = 0), 100);
          originOnChangeRef.current?.(v);
        })
      : (v: string) => originOnChangeRef.current?.(v)
  );

  const contains = (parent: HTMLElement, descendant: HTMLElement) => {
    try {
      // Firefox inserts inaccessible nodes around video elements
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      descendant.parentNode;
    } catch (e) {
      return false;
    }
    return parent.contains(descendant);
  };

  const tryBindTimestampListener = () => {
    if (listenerBoundRef.current) return;

    const editor = (editorRef.current as any)?.getEditor?.();
    if (!editor) return;

    if (props.readOnly || !props.autoTimestamp) {
      listenerRef.current = null;
      return;
    }

    const formatTimestamp = () => {
      const d = new Date();
      const pad = (v: number) => `${v}`.padStart(2, "0");
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
        d.getMinutes()
      )}`;
    };

    const updateTimestampForLine = (newlineIndex: number, checked: boolean, preserveSelection: boolean = true) => {
      const [line, offset] = editor.getLine(newlineIndex);
      if (!line) return;

      const lineStart = newlineIndex - offset;
      const lineLength = line.length();
      const lineDelta = editor.getContents(lineStart, lineLength);
      const ops = lineDelta.ops || [];
      let offsetInLine = 0;
      let timestampOffset: number | null = null;
      ops.forEach((op: any) => {
        const insert = op.insert;
        const length =
          typeof insert === "string"
            ? insert.length
            : insert
            ? 1
            : 0;
        if (
          insert &&
          typeof insert === "object" &&
          Object.prototype.hasOwnProperty.call(insert, "timestamp")
        ) {
          timestampOffset = offsetInLine;
        }
        offsetInLine += length;
      });

      const selection = editor.getSelection();
      if (checked) {
        const tsValue = formatTimestamp();
        if (timestampOffset !== null) {
          const globalIndex = lineStart + timestampOffset;
          editor.deleteText(globalIndex, 1, "silent");
          editor.insertEmbed(globalIndex, "timestamp", tsValue, "silent");
        } else {
          const lineEndIndex = lineStart + lineLength - 1;
          editor.insertEmbed(lineEndIndex, "timestamp", tsValue, "silent");
        }
      } else if (timestampOffset !== null) {
        const globalIndex = lineStart + timestampOffset;
        editor.deleteText(globalIndex, 1, "silent");
      }

      // 只有在 preserveSelection 为 true 时才恢复选择位置
      if (preserveSelection && selection) {
        editor.setSelection(selection, "silent");
      }
    };

    const handler = (delta: any, _old: any, source: string) => {
      if (source !== "user") return;
      
      // 如果正在处理回车键，不恢复选择位置，避免光标跳动
      const preserveSelection = !isHandlingEnterRef.current;
      
      let index = 0;
      delta.ops?.forEach((op: any) => {
        if (op.retain) {
          const length = typeof op.retain === "number" ? op.retain : 0;
          const listAttr = op.attributes?.list;
          if (listAttr === "checked" || listAttr === "unchecked") {
            const newlineIndex = index + length - 1;
            updateTimestampForLine(newlineIndex, listAttr === "checked", preserveSelection);
          }
          index += length;
        } else if (typeof op.insert === "string") {
          index += op.insert.length;
        } else if (op.insert) {
          index += 1;
        }
      });
    };
    
    // 处理回车键按下事件
    const handleEnterKey = (e: KeyboardEvent) => {
      // 只处理回车键
      if (e.key !== "Enter" || e.shiftKey || e.ctrlKey || e.metaKey || e.altKey) {
        return;
      }
      
      // 设置标志，告诉 handler 不要恢复选择位置
      isHandlingEnterRef.current = true;
      
      // 延迟处理，确保 Quill 先完成换行操作
      setTimeout(() => {
        try {
          const selection = editor.getSelection();
          if (!selection) return;
          
          const currentIndex = selection.index;
          let targetCursorIndex = currentIndex;
          
          // 先检查上一行（换行前的原行）是否是 checked 状态
          if (currentIndex > 0) {
            const prevIndex = currentIndex - 1;
            const [prevLine, prevOffset] = editor.getLine(prevIndex);
            if (prevLine) {
              const prevLineIndex = prevIndex - prevOffset;
              const prevLineStart = prevLineIndex;
              const prevLineLength = prevLine.length();
              const prevLineDelta = editor.getContents(prevLineStart, prevLineLength);
              const prevLineOps = prevLineDelta.ops || [];
              
              let prevLineIsChecked = false;
              prevLineOps.forEach((lineOp: any) => {
                if (lineOp.attributes?.list === "checked") {
                  prevLineIsChecked = true;
                }
              });
              
              // 如果上一行是 checked 状态，更新时间戳但不恢复选择位置
              if (prevLineIsChecked) {
                updateTimestampForLine(prevLineIndex, true, false);
                return
              }
            }
          }
          
          // 然后检查当前行（换行后的新行）是否是 checked 状态
          const [currentLine, currentOffset] = editor.getLine(currentIndex);
          if (currentLine) {
            const currentLineIndex = currentIndex - currentOffset;
            const currentLineStart = currentLineIndex;
            const currentLineLength = currentLine.length();
            const currentLineDelta = editor.getContents(currentLineStart, currentLineLength);
            const currentLineOps = currentLineDelta.ops || [];
            
            let currentLineIsChecked = false;
            currentLineOps.forEach((lineOp: any) => {
              if (lineOp.attributes?.list === "checked") {
                currentLineIsChecked = true;
              }
            });
            
            // 如果当前行是 checked 状态，确保有时间戳
            if (currentLineIsChecked) {
              updateTimestampForLine(currentLineIndex, true, false);
              
              // 重新获取当前行信息（因为添加时间戳后行长度可能变化）
              const [updatedCurrentLine, updatedCurrentOffset] = editor.getLine(currentIndex);
              if (updatedCurrentLine) {
                const updatedCurrentLineIndex = currentIndex - updatedCurrentOffset;
                const updatedCurrentLineStart = updatedCurrentLineIndex;
                const updatedCurrentLineLength = updatedCurrentLine.length();
                const updatedCurrentLineDelta = editor.getContents(updatedCurrentLineStart, updatedCurrentLineLength);
                const updatedCurrentLineOps = updatedCurrentLineDelta.ops || [];
                
                // 找到时间戳的位置和文本长度
                let textLengthBeforeTimestamp = 0;
                let hasTimestamp = false;
                updatedCurrentLineOps.forEach((op: any) => {
                  const insert = op.insert;
                  if (
                    insert &&
                    typeof insert === "object" &&
                    Object.prototype.hasOwnProperty.call(insert, "timestamp")
                  ) {
                    hasTimestamp = true;
                    return;
                  }
                  if (!hasTimestamp) {
                    const length =
                      typeof insert === "string"
                        ? insert.length
                        : insert
                        ? 1
                        : 0;
                    textLengthBeforeTimestamp += length;
                  }
                });
                
                // 计算目标光标位置：如果新行有文本，光标应该在文本末尾（时间戳之前）
                // 如果新行只有时间戳，光标应该在行首
                targetCursorIndex = textLengthBeforeTimestamp > 0 
                  ? updatedCurrentLineStart + textLengthBeforeTimestamp 
                  : updatedCurrentLineStart;
              }
            } else {
              // 如果当前行不是 checked 状态，光标应该在新行的开始位置
              targetCursorIndex = currentLineStart;
            }
          }
          
          // 最后，明确将光标设置到新行的目标位置
          // 使用 requestAnimationFrame 确保在所有操作完成后设置光标
          requestAnimationFrame(() => {
            try {
              editor.setSelection(targetCursorIndex, 0, "silent");
              // 清除标志，允许后续的 text-change 事件正常恢复选择位置
              setTimeout(() => {
                isHandlingEnterRef.current = false;
              }, 100);
            } catch (e) {
              console.warn("Error setting cursor position:", e);
              isHandlingEnterRef.current = false;
            }
          });
        } catch (e) {
          // 忽略错误，避免影响正常编辑
          console.warn("Error handling enter key:", e);
          isHandlingEnterRef.current = false;
        }
      }, 0);
    };

    const mouseHandler = (event: MouseEvent) => {
      const root = editor.root as HTMLElement | undefined;
      if (!root) return;
      const target = event.target as HTMLElement | null;
      if (!target || !root.contains(target)) return;
      const quillAny = Quill as any;
      const lineElement = target.closest("p, li, div") as HTMLElement | null;
      if (!lineElement || !root.contains(lineElement)) return;
      const timestampNode = lineElement.querySelector(".ql-timestamp") as HTMLElement | null;
      if (!timestampNode) return;
      const tsRect = timestampNode.getBoundingClientRect();
      if (event.clientX < tsRect.left) return;
      event.preventDefault();
      event.stopPropagation();
      const blot = quillAny.find(timestampNode);
      if (!blot) return;
      const index = editor.getIndex(blot);
      editor.setSelection(index, 0, "silent");
    };

    editor.on("text-change", handler);
    editor.root.addEventListener("mousedown", mouseHandler);
    editor.root.addEventListener("keydown", handleEnterKey);
    listenerRef.current = { handler, mouseHandler, keyHandler: handleEnterKey };
    listenerBoundRef.current = true;
  };

  const handleChange = (value: string) => {
    setContent(value);
    tryBindTimestampListener();
    onChangeRef.current(value);
  };

  useEffect(() => {
    let finalValue = props.value;
    if (!/^<\w+>.+<\/\w+>$/.test(props.value)) {
      finalValue = `<p class="">${props.value}</p>`;
    }
    setContent(finalValue);
  }, [props.value]);

  const handleClickWrapper = (e: React.MouseEvent<HTMLDivElement>) => {
    const editor = (editorRef.current as any)?.getEditor?.();
    if (!editor) return;
    if (editor.theme?.pickers) {
      editor.theme.pickers.forEach((i: any) => {
        if (!contains(i.container, e.nativeEvent.target as HTMLElement)) {
          i.close();
        }
      });
    }
  };

  const id = "rtf-editor";
  const Wrapper = props.autoHeight ? AutoHeightReactQuill : FixHeightReactQuill;

  useEffect(() => {
    const editor = (editorRef.current as any)?.getEditor?.();
    return () => {
      if (editor && listenerBoundRef.current) {
        editor.off("text-change");
        if (listenerRef.current?.mouseHandler) {
          editor.root.removeEventListener("mousedown", listenerRef.current.mouseHandler);
        }
        if (listenerRef.current?.keyHandler) {
          editor.root.removeEventListener("keydown", listenerRef.current.keyHandler);
        }
        listenerBoundRef.current = false;
        listenerRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    const editor = (editorRef.current as any)?.getEditor?.();
    if (!editor) return;
    editor.root.dataset.placeholder = props.placeholder;
  }, [props.placeholder]);

  return (
    <Wrapper
      id={id}
      onClick={handleClickWrapper}
      ref={wrapperRef}
      $hideToolbar={props.hideToolbar}
      $style={props.$style}
    >
      <Suspense fallback={<Skeleton />}>
        <ReactQuillEditor
          ref={editorRef}
          bounds={`#${id}`}
          modules={{
            toolbar: toolbarOptions,
          }}
          theme="snow"
          value={content}
          placeholder={props.placeholder}
          readOnly={props.readOnly}
          onChange={handleChange}
        />
      </Suspense>
    </Wrapper>
  );
}

const RichTextEditorCompBase = new UICompBuilder(childrenMap, (props) => {
  const handleChange = (v: string) => {
    props.value.onChange(v);
    props.onEvent("change");
  };

  return (
    <RichTextEditor
      autoHeight={props.autoHeight}
      hideToolbar={props.hideToolbar}
      readOnly={props.readOnly}
      autoTimestamp={props.autoTimestamp}
      value={props.value.value}
      placeholder={props.placeholder}
      onChange={handleChange}
      $style={props.style}
    />
  );
})
  .setPropertyViewFn((children) => {
    return (
      <>
        <Section name={sectionNames.basic}>
          {children.value.propertyView({ label: trans("prop.defaultValue") })}
          {placeholderPropertyView(children)}
          {children.autoTimestamp.propertyView({ label: trans("richTextEditor.autoTimestamp") })}
        </Section>
        <FormDataPropertyView {...children} />
        <Section name={sectionNames.interaction}>
          {children.onEvent.getPropertyView()}
          {readOnlyPropertyView(children)}
        </Section>
        <Section name={sectionNames.layout}>
          {children.hideToolbar.propertyView({ label: trans("richTextEditor.hideToolbar") })}
          {children.autoHeight.getPropertyView()}
          {hiddenPropertyView(children)}
        </Section>
        <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
      </>
    );
  })
  .build();

class RichTextEditorCompAutoHeight extends RichTextEditorCompBase {
  override autoHeight(): boolean {
    return this.children.autoHeight.getView();
  }
}

export const RichTextEditorComp = withExposingConfigs(RichTextEditorCompAutoHeight, [
  new NameConfig("value", trans("export.richTextEditorValueDesc")),
  new NameConfig("readOnly", trans("export.richTextEditorReadOnlyDesc")),
  new NameConfig("hideToolbar", trans("export.richTextEditorHideToolBarDesc")),
  depsConfig({
    name: "completedCount",
    desc: trans("export.richTextEditorCompletedCountDesc" as any),
    depKeys: ["value"],
    func: (input) => {
      const value = (input.value as string) || "";
      return countChecklistItems(value).completed;
    },
  }),
  depsConfig({
    name: "uncompletedCount",
    desc: trans("export.richTextEditorUncompletedCountDesc" as any),
    depKeys: ["value"],
    func: (input) => {
      const value = (input.value as string) || "";
      return countChecklistItems(value).uncompleted;
    },
  }),
  NameConfigHidden,
]);
