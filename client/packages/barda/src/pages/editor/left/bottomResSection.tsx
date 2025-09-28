import { trans } from "@barda/i18n";
import { BaseSection } from "barda-design";
import { CompInfo } from "comps/editorState";
import { memo } from "react";
import { BottomResTypeEnum } from "types/bottomRes";
import { CollapseView } from "../LeftContent";

interface BottomResSectionProps {
    bottomResList: Array<CompInfo>;
    selectedBottomResName: string | undefined;
    onBottomResClick: (name: string, type?: BottomResTypeEnum) => void;
}

export const BottomResSection = memo(({ bottomResList, selectedBottomResName, onBottomResClick }: BottomResSectionProps) => {
    const bottomResCollapse = bottomResList.map((item) => {
        return <CollapseView
            key={item.name}
            name={item.name}
            desc={item.dataDesc}
            data={item.data}
            isSelected={selectedBottomResName === item.name}
            onClick={onBottomResClick}
            resType={item.type as BottomResTypeEnum} />
    });

    return (
        <BaseSection name={trans("leftPanel.queries")} width={288} noMargin>
            <span>{bottomResCollapse}</span>
        </BaseSection>
    );
}); 