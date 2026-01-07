import { Section, sectionNames } from "barda-design";
import { getFormatter } from "base/codeEditor/autoFormat";
import {
  EditorState,
  EditorView,
  type EditorView as EditorViewType,
} from "base/codeEditor/codeMirror";
import { useExtensions } from "base/codeEditor/extensions";
import { BoolControl } from "comps/controls/boolControl";
import { jsonValueExposingStateControl } from "comps/controls/codeStateControl";
import { ChangeBlurEventHandlerControl } from "comps/controls/eventHandlerControl";
import { LabelControl } from "comps/controls/labelControl";
import { styleControl } from "comps/controls/styleControl";
import { JsonEditorStyle } from "comps/controls/styleControlConstants";
import { migrateOldData, withDefault } from "comps/generators/simpleGenerators";
import { hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import { useEffect, useRef } from "react";
import styled from "styled-components";
import { UICompBuilder } from "../../generators";
import {
  NameConfig,
  NameConfigHidden,
  withExposingConfigs,
} from "../../generators/withExposing";
import {
  formDataChildren,
  FormDataPropertyView,
} from "../formComp/formDataConstants";
import { defaultData } from "./jsonConstants";

/**
 * JsonEditor Comp
 */

const Wrapper = styled.div`
  background-color: #fff;
  border: 1px solid #d7d9e0;
  border-radius: 4px;
  overflow: auto;
  height: 100%;
`;

/**
 * Compatible with old data 2022-10-19
 */
function fixOldData(oldData: any) {
  if (oldData && !oldData.hasOwnProperty("label")) {
    return {
      ...oldData,
      label: {
        text: "",
      },
    };
  }
  return oldData;
}

/**
 * Compatible with old data 2022-11-18
 */
function fixOldDataSecond(oldData: any) {
  if (oldData && oldData.hasOwnProperty("default")) {
    return {
      ...oldData,
      value: oldData.default,
    };
  }
  return oldData;
}

const childrenMap = {
  value: jsonValueExposingStateControl("value", defaultData),
  onEvent: ChangeBlurEventHandlerControl,
  label: withDefault(LabelControl, { position: "column" }),
  style: styleControl(JsonEditorStyle),
  autoFormat: BoolControl,

  ...formDataChildren,
};

// 格式化内容的公共函数
const formatContent = async (content: string, autoFormat: boolean, extensions: any) => {
  if (!autoFormat) {
    return content;
  }
  
  const formatter = getFormatter("json", "PureJSON");
  if (!formatter) {
    return content;
  }
  
  try {
    return await formatter(content);
  } catch {
    return content;
  }
};

// 创建编辑器状态的公共函数
const createEditorState = (content: string, extensions: any) => {
  return EditorState.create({
    doc: content,
    extensions,
  });
};

let JsonEditorTmpComp = (function () {
  return new UICompBuilder(childrenMap, (props) => {
    const wrapperRef = useRef<HTMLDivElement>(null);
    const view = useRef<EditorViewType | null>(null);
    const isUserEditing = useRef<boolean>(false);
    const { extensions } = useExtensions({
      codeType: "PureJSON",
      language: "json",
      showLineNum: true,
      enableClickCompName: false,
      onFocus: (focused) => {
        if (focused) {
          wrapperRef.current?.click();
        } else {
          if (props.autoFormat && view.current) {
            const currentContent = view.current.state.doc.toString();
              const formatter = getFormatter("json", "PureJSON");
              if (formatter) {
                formatter(currentContent)
                  .then((formattedContent) => {
                    if (formattedContent !== currentContent && view.current) {
                      const state = EditorState.create({
                        doc: formattedContent,
                        extensions,
                      });
                      view.current.setState(state);
                    }
                  })
                  .catch(() => {
                  });
              }
          }
          props.onEvent("blur");
        }
      },
      onChange: (state) => {
        isUserEditing.current = true; // 标记用户正在编辑
        try {
          const value = JSON.parse(state.doc.toString());
          props.value.onChange(value);
          props.onEvent("change");
        } catch (error) {
          // JSON 解析错误时保持当前状态
        }
      },
    });

    useEffect(() => {
      // 当外部值变化时，同步到编辑器
      if (view.current && !isUserEditing.current) {
        const newContent = JSON.stringify(props.value.value, null, 2);
        const currentContent = view.current.state.doc.toString();
        
        // 只有当内容真正不同时才更新
        if (newContent !== currentContent) {
          formatContent(newContent, props.autoFormat, extensions)
            .then((formattedContent) => {
              const state = createEditorState(formattedContent, extensions);
              view.current?.setState(state);
            });
        }
      }
      
      // 重置编辑状态
      isUserEditing.current = false;
    }, [props.value.value, extensions, props.autoFormat]);

    // 初始化编辑器
    useEffect(() => {
      if (wrapperRef.current && !view.current) {
        const initialContent = JSON.stringify(props.value.value, null, 2);
        
        formatContent(initialContent, props.autoFormat, extensions)
          .then((formattedContent) => {
            const state = createEditorState(formattedContent, extensions);
            view.current = new EditorView({ state, parent: wrapperRef.current! });
          });
      }
    }, [extensions, props.value.value, props.autoFormat]);

    return props.label({
      style: props.style,
      children: <Wrapper ref={wrapperRef} />,
    });
  })
    .setPropertyViewFn((children) => {
      return (
        <>
          <Section name={sectionNames.basic}>
            {children.value.propertyView({ label: trans("prop.defaultValue") })}
          </Section>
          <FormDataPropertyView {...children} />
          {children.label.getPropertyView()}
          <Section name={sectionNames.interaction}>
            {children.onEvent.getPropertyView()}
          </Section>
          <Section name={sectionNames.advanced}>
            {children.autoFormat.propertyView({
              label: trans("export.jsonEditorAutoFormat"),
              tooltip: trans("export.jsonEditorAutoFormatDesc"),
            })}
          </Section>
          <Section name={sectionNames.layout}>
            {hiddenPropertyView(children)}
          </Section>
          <Section name={sectionNames.style}>
            {children.style.getPropertyView()}
          </Section>
        </>
      );
    })
    .build();
})();

JsonEditorTmpComp = migrateOldData(JsonEditorTmpComp, fixOldData);

JsonEditorTmpComp = migrateOldData(JsonEditorTmpComp, fixOldDataSecond);

JsonEditorTmpComp = class extends JsonEditorTmpComp {
  override autoHeight(): boolean {
    return false;
  }
};

export const JsonEditorComp = withExposingConfigs(JsonEditorTmpComp, [
  new NameConfig("value", trans("export.jsonEditorDesc")),
  NameConfigHidden,
]);
