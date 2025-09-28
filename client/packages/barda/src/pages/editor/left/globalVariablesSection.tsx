import { trans } from "@barda/i18n";
import { BaseSection } from "barda-design";
import { CompInfo } from "comps/editorState";
import { hookCompCategory } from "comps/hooks/hookCompTypes";
import _ from "lodash";
import { memo } from "react";
import { CollapseView } from "../LeftContent";

interface GlobalVariablesSectionProps {
    globalVariablesInfoList: Array<CompInfo>;
}

export const GlobalVariablesSection = memo(({ globalVariablesInfoList }: GlobalVariablesSectionProps) => {
    const globalVariablesList = _.sortBy(
        globalVariablesInfoList.filter((info: CompInfo) => hookCompCategory(info.type) === "hook"),
        [(x) => x.name]
    ).map((item) => (
        <CollapseView
            key={item.name}
            name={item.name}
            desc={item.dataDesc}
            data={item.data}
            isSelected={false}
            onClick={_.noop}
        />
    ));

    return (
        <BaseSection
            name={trans("leftPanel.globals")}
            width={288}
            noMargin
        >
            <span>{globalVariablesList}</span>
        </BaseSection>
    );
})