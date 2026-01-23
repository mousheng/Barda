import {
  BoolCodeControl,
  NumberControl,
  StringControl,
} from "@barda/comps/controls/codeControl";
import { Section, sectionNames } from "barda-design";
import { numberExposingStateControl } from "@barda/comps/controls/codeStateControl";
import { withDefault } from "comps/generators";
import { RecordConstructorToComp } from "barda-core";
import { trans } from "i18n";
import { dropdownControl } from "comps/controls/dropdownControl";

const budgeSizeOptions = [
  {
    label: trans("prop.budgeSizeDefault"),
    value: "default",
  },
  {
    label: trans("prop.budgeSizeSmall"),
    value: "small",
  },
] as const;

const budgeTypeOptions = [
  {
    label: trans("prop.number"),
    value: "number",
  },
  {
    label: trans("prop.dot"),
    value: "dot",
  },
] as const;

export const budgeChildren = {
  budgeType: dropdownControl(budgeTypeOptions, "number"),
  budgeCount: withDefault(numberExposingStateControl("budgeCount"), "0"),
  budgeSize: dropdownControl(budgeSizeOptions, "default"),
  showZero: BoolCodeControl,
  overflowCount: withDefault(NumberControl, 99),
  budgeTitle: withDefault(StringControl, ""),
};

type BudgeComp = RecordConstructorToComp<typeof budgeChildren>;

export const BudgeBasicSection = (children: BudgeComp) => (
  <Section name={sectionNames.badge}>
    {children.budgeType.propertyView({
      label: trans("prop.budgeType"),
      radioButton: true,
    })}
    {children.budgeCount.propertyView({ label: trans("prop.budgeCount") })}
    {children.budgeType.getView() === "number" &&
      children.overflowCount.propertyView({ label: trans("prop.overflowCount") })}
    {children.budgeType.getView() === "number" &&
      children.budgeSize.propertyView({ label: trans("prop.budgeSize") })}
    {children.budgeTitle.propertyView({ label: trans("prop.budgeTitle") })}
  </Section>
);
