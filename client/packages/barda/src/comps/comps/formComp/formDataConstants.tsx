import { RecordConstructorToComp } from "barda-core";
import { StringControl } from "comps/controls/codeControl";
import { CompNameContext } from "comps/editorState";
import { useFindUIParentContainer } from "comps/editorSelectors";
import { Section } from "barda-design";
import { ReactNode, useContext } from "react";
import { trans } from "i18n";

export interface IForm {
  onEventPropertyView(title: ReactNode): ReactNode;
  submit(): Promise<void>;
  disableSubmit(): boolean;
}

export const formDataChildren = {
  formDataKey: StringControl,
};

type FormDataComp = RecordConstructorToComp<typeof formDataChildren>;

export const FormDataPropertyView = (children: FormDataComp) => {
  const name = useContext(CompNameContext);
  const parentContainer = useFindUIParentContainer(name, "form");
  if (!parentContainer) {
    return null;
  }
  return (
    <Section name={trans("form")}>
      {children.formDataKey.propertyView({
        label: trans("formComp.name"),
        placeholder: name,
        tooltip: trans("formComp.nameTooltip"),
      })}
    </Section>
  );
};
