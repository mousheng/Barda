import { Layers } from "@barda/constants/Layers";
import { TabsProps } from "antd";
import { AttributeIcon, InsertIcon, RightPanelTabs } from "barda-design";
import UIComp from "comps/comps/uiComp";
import { trans } from "i18n";
import { memo, useCallback, useEffect, useMemo, useState } from "react";
import { isAggregationApp } from "util/appUtils";
import InsertView from "./InsertView";
import PropertyView from "./PropertyView";

type RightPanelProps = {
  onTabChange: (key: string) => void;
  onCompDrag: (dragCompKey: string) => void;
  showPropertyPane: boolean;
  uiComp?: InstanceType<typeof UIComp>;
};


const RightPanel = memo((props: RightPanelProps) => {
  const { onTabChange, showPropertyPane, uiComp, onCompDrag } = props;
  const [activeKey, setActiveKey] = useState("insert");

  const aggregationApp = useMemo(() =>
    uiComp && isAggregationApp(uiComp.children.compType.getView()),
    [uiComp]
  );

  const tabConfigs = useMemo(() => {
    const configs: TabsProps['items'] = [
      {
        key: "property",
        label: trans("rightPanel.propertyTab"),
        icon: <AttributeIcon />,
        children: <PropertyView uiComp={uiComp} />,
      },
    ];

    if (!aggregationApp) {
      configs!.push({
        key: "insert",
        label: trans("rightPanel.createTab"),
        icon: <InsertIcon />,
        children: <InsertView onCompDrag={onCompDrag} />,
      });
    }

    return configs;
  }, [aggregationApp, uiComp, onCompDrag]);

  const handleTabChange = useCallback((key: string) => {
    onTabChange(key);
  }, [onTabChange]);

  const handleActiveKeyChange = useCallback((key: string) => {
    setActiveKey(key);
  }, []);

  useEffect(() => {
    const key = aggregationApp || showPropertyPane ? "property" : "insert";
    if (key !== activeKey) {
      handleActiveKeyChange(key);
    }
  }, [showPropertyPane, aggregationApp, activeKey, handleActiveKeyChange]);

  return (
    <RightPanelTabs
      $zIndex={Layers.rightPanel}
      onChange={handleTabChange}
      items={tabConfigs}
      activeKey={activeKey}
    />
  );
});

export default RightPanel;
