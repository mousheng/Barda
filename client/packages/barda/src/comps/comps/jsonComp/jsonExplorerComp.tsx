import { Section, sectionNames } from "barda-design";
import { UICompBuilder, withDefault } from "../../generators";
import { NameConfigHidden, NameConfig, withExposingConfigs } from "../../generators/withExposing";
import { VirtualJsonTree } from "../../../components/resultPanel/VirtualJsonTree";
import { defaultData } from "./jsonConstants";
import styled from "styled-components";
import { BoolControl } from "comps/controls/boolControl";
import { dropdownControl } from "comps/controls/dropdownControl";
import { ArrayOrJSONObjectControl } from "comps/controls/codeControl";
import { hiddenPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import { styleControl } from "comps/controls/styleControl";
import { JsonExplorerStyle } from "comps/controls/styleControlConstants";

const themeOptions = [
  { label: "default", value: "default" },
  { label: "a11y", value: "a11y" },
  { label: "github", value: "github" },
  { label: "vscode", value: "vscode" },
  { label: "atom", value: "atom" },
  { label: "winter-is-coming", value: "winter-is-coming" },
];

const JsonExplorerContainer = styled.div<{ $background: string, $border: string, $radius: string }>`
  height: 100%;
  overflow: hidden;
  background-color: ${(props) => props.$background};
  border: 1px solid ${props => props.$border};
  border-radius: ${props => props.$radius};
  padding: 4px 0;
`;

let JsonExplorerTmpComp = (function () {
  const childrenMap = {
    value: withDefault(ArrayOrJSONObjectControl, JSON.stringify(defaultData, null, 2)),
    expandToggle: BoolControl.DEFAULT_TRUE,
    enableClipboard: BoolControl.DEFAULT_TRUE,
    theme: dropdownControl(themeOptions, "default"),
    style: styleControl(JsonExplorerStyle),
  };
  return new UICompBuilder(childrenMap, (props) => (
    <JsonExplorerContainer
      $background={props.style.background}
      $border={props.style.border}
      $radius={props.style.radius}
    >
      <VirtualJsonTree
        src={props.value}
        collapsed={!props.expandToggle}
        enableClipboard={props.enableClipboard}
        collapseStringsAfterLength={200}
      />
    </JsonExplorerContainer>
  ))
    .setPropertyViewFn((children) => {
      return (
        <>
          <Section name={sectionNames.basic}>
            {children.value.propertyView({
              label: trans("data"),
            })}
            {children.expandToggle.propertyView({ label: trans("jsonExplorer.expandToggle") })}
            {children.enableClipboard.propertyView({ label: trans("jsonExplorer.enableClipboard") })}
          </Section>

          <Section name={sectionNames.style}>
            {children.style.getPropertyView()}
          </Section>

          <Section name={sectionNames.layout}>{hiddenPropertyView(children)}</Section>
        </>
      );
    })
    .build();
})();

JsonExplorerTmpComp = class extends JsonExplorerTmpComp {
  override autoHeight(): boolean {
    return false;
  }
};

export const JsonExplorerComp = withExposingConfigs(JsonExplorerTmpComp, [
  new NameConfig("value", trans("jsonExplorer.valueDesc")),
  NameConfigHidden,
]);
