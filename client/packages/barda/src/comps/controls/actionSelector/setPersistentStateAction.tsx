import { APP_STORE_NAMESPACE } from "@barda/comps/hooks/localStorageComp";
import { AutoComplete } from "antd";
import { BranchDiv } from "barda-design";
import { JSONValueControl, StringControl } from "comps/controls/codeControl";
import { dropdownControl } from "comps/controls/dropdownControl";
import { MultiCompBuilder } from "comps/generators/multi";
import { trans } from "i18n";
import { useMemo } from "react";

const OperationOptions = [
  { label: trans("eventHandler.set"), value: "set" },
  { label: trans("eventHandler.remove"), value: "remove" },
  { label: trans("eventHandler.clear"), value: "clear" },
] as const;

function getCurrentKeys(): string[] {
  try {
    const originStore = localStorage.getItem(APP_STORE_NAMESPACE) || "{}";
    const parseStore = JSON.parse(originStore);
    return Object.keys(parseStore);
  } catch (e) {
    return [];
  }
}

// 自定义Key选择器组件
function KeySelector(props: { value: string; onChange: (value: string) => void }) {
  const { value, onChange } = props;
  const currentKeys = useMemo(() => getCurrentKeys(), []);

  // 创建AutoComplete的选项
  const options = useMemo(() => {
    return currentKeys.map(key => ({
      value: key,
      label: key,
    }));
  }, [currentKeys]);

  return (
    <AutoComplete
      value={value}
      onChange={onChange}
      options={options}
      placeholder={trans("eventHandler.selectOrInputKey")}
      allowClear
      style={{ width: "100%" }}
      filterOption={(inputValue, option) => {
        return option?.value?.toLowerCase().includes(inputValue.toLowerCase()) ?? false;
      }}
    />
  );
}

const SetPersistentStateActionBase = (function () {
  const childrenMap = {
    operation: dropdownControl(OperationOptions, "set"),
    key: StringControl,
    value: JSONValueControl,
  };
  
  return new MultiCompBuilder(childrenMap, (props) => {
    return () => {
      const { operation, key, value } = props;
      
      try {
        const originStore = localStorage.getItem(APP_STORE_NAMESPACE) || "{}";
        
        if (operation === "clear") {
          localStorage.removeItem(APP_STORE_NAMESPACE);
          return;
        }
        
        if (operation === "remove") {
          if (typeof key === "string" && key.length > 0) {
            const parseStore = JSON.parse(originStore);
            delete parseStore[key];
            localStorage.setItem(APP_STORE_NAMESPACE, JSON.stringify(parseStore));
          }
          return;
        }
        
        if (operation === "set") {
          if (typeof key === "string" && key.length > 0) {
            if (originStore.length > 1024 * 1024) {
              return; // Limit up to 1m
            }
            
            const parseStore = JSON.parse(originStore);
            parseStore[key] = value;
            localStorage.setItem(APP_STORE_NAMESPACE, JSON.stringify(parseStore));
          }
        }
      } catch (e) {
        localStorage.setItem(APP_STORE_NAMESPACE, "{}");
      }
    };
  })
  .setPropertyViewFn((children) => (
    <>
      <BranchDiv $type={"inline"}>
        {children.operation.propertyView({
          label: trans("eventHandler.operation"),
        })}
      </BranchDiv>
      {children.operation.getView() !== "clear" && (
        <BranchDiv>
          <KeySelector
            value={children.key.getView()}
            onChange={(value) => children.key.dispatchChangeValueAction(value)}
          />
        </BranchDiv>
      )}
      {children.operation.getView() === "set" && (
        <BranchDiv>
          {children.value.propertyView({
            label: trans("eventHandler.value"),
            layout: "vertical",
            placeholder: "myValue",
          })}
        </BranchDiv>
      )}
    </>
  ))
  .build();
})();

// 为SetPersistentStateAction添加displayName方法
export class SetPersistentStateAction extends SetPersistentStateActionBase {
  displayName() {
    const operation = this.children.operation.getView();
    const key = this.children.key.getView();
    
    // 如果是清空操作，不需要key
    if (operation === "clear") {
      return trans("eventHandler.clear");
    }
    
    // 如果是设置或删除操作，需要key
    if (operation === "set" || operation === "remove") {
      if (!key || key.length === 0) {
        return undefined; // 返回undefined会显示"选择不完整"
      }
      
      const operationLabel = operation === "set" 
        ? trans("eventHandler.set") 
        : trans("eventHandler.remove");
      
      return `${operationLabel}: ${key}`;
    }
    
    return undefined;
  }
}
