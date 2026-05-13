import { PopupCard } from "barda-design";
import { Input } from "barda-design";
import { useRootComp, useNameAndExposingInfo } from "comps/editorSelectors";
import { valueComp } from "comps/generators";
import { ControlPropertyViewWrapper } from "barda-design";
import { trans } from "i18n";
import { ReactNode, useRef, useState } from "react";
import { ControlParams } from "./controlParams";
import { checkName } from "comps/utils/rename";
import { renameAction } from "barda-core";

interface PropertyViewProps {
  comp: CompNameControl;
  onValidate?: (preValue: string, nextValue: string) => string;
  onFinish?: (preValue: string, nextValue: string) => void;
}

function PropertyView(props: PropertyViewProps) {
  const { comp, onValidate } = props;
  const [error, setError] = useState("");
  const rootComp = useRootComp();
  const nameAndExposingInfo = useNameAndExposingInfo();
  const prevName = comp.getView();
  const originNameRef = useRef(prevName);

  const checkRenameLocal = (oldName: string, name: string): string => {
    const nameError = checkName(name);
    if (nameError) return nameError;
    if (name !== oldName && nameAndExposingInfo.hasOwnProperty(name)) {
      return trans("comp.nameExists", { name });
    }
    return "";
  };

  const handleFinish = (nextName: string) => {
    if (error) {
      comp.dispatchChangeValueAction(originNameRef.current);
      return;
    }
    if (!onValidate) {
      const renameError = checkRenameLocal(originNameRef.current, nextName);
      if (renameError) return;
      if (nextName !== originNameRef.current) {
        rootComp?.dispatch(renameAction(originNameRef.current, nextName));
      }
    }
    comp.dispatchChangeValueAction(nextName);
    originNameRef.current = nextName;
    props.onFinish && props.onFinish(prevName, nextName);
  };

  return (
    <div>
      <Input
        value={prevName}
        onFocus={() => {
          originNameRef.current = prevName;
        }}
        onBlur={(e) => handleFinish(e.target.value)}
        onPressEnter={(e) => handleFinish((e.target as HTMLInputElement).value)}
        onChange={(e) => {
          const nextName = e.target.value;
          if (onValidate) {
            setError(onValidate(originNameRef.current, nextName));
            return;
          }
          setError(checkRenameLocal(prevName, nextName));
        }}
      />
      <PopupCard
        editorFocus={!!error}
        title={error ? trans("error") : ""}
        content={error}
        hasError={!!error}
      />
    </div>
  );
}

class CompNameControl extends valueComp<string>("") {
  getPropertyView(): ReactNode {
    return <PropertyView comp={this} />;
  }

  propertyView(
    params: ControlParams & {
      onValidate?: (pre: string, value: string) => string;
      onFinish?: (preValue: string, nextValue: string) => void;
    }
  ): JSX.Element {
    const { onValidate, ...otherParams } = params;
    return (
      <ControlPropertyViewWrapper {...otherParams}>
        <PropertyView comp={this} onValidate={onValidate} onFinish={params.onFinish} />
      </ControlPropertyViewWrapper>
    );
  }
}

export default CompNameControl;
