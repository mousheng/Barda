import { getBottomResIcon } from "@barda/util/bottomResUtils";
import { CompAction, CompActionTypes } from "barda-core";
import {
  DocLink,
  QueryConfigLabel,
  QueryConfigWrapper,
  QueryPropertyViewWrapper,
  QuerySectionWrapper,
} from "barda-design";
import { JSONValueControl } from "comps/controls/codeControl";
import { EventConfigType, eventHandlerControl } from "comps/controls/eventHandlerControl";
import { MultiCompBuilder, valueComp } from "comps/generators";
import { bottomResListComp } from "comps/generators/bottomResList";
import { NameConfig, withExposingConfigs } from "comps/generators/withExposing";
import { trans } from "i18n";
import _ from "lodash";
import { BottomTabs } from "pages/editor/bottom/BottomTabs";
import { ReactNode, useMemo } from "react";
import { BottomResComp, BottomResCompResult, BottomResTypeEnum } from "types/bottomRes";
import { setFieldsNoTypeCheck } from "util/objectUtils";
import { QueryTutorials } from "util/tutorialUtils";
import { SimpleNameComp } from "./simpleNameComp";

const dataChangeEvent: EventConfigType = {
  label: "onDataChange",
  value: "dataChange",
  description: "",
};

const DataResponderItemCompBase = new MultiCompBuilder(
  {
    name: SimpleNameComp,
    order: valueComp<number>(0),
    commentary: valueComp<string>(""),
    onEvent: eventHandlerControl([dataChangeEvent], "query"),
    data: JSONValueControl,
  },
  () => null
)
  .setPropertyViewFn((children) => {
    const tabsConfig = useMemo(() => [
      {
        key: "general",
        title: trans("query.generalTab"),
        children: (
          <QueryPropertyViewWrapper>
            <QuerySectionWrapper>
              {children.data.propertyView({
                label: trans("dataResponder.data"),
                tooltip: trans("dataResponder.dataTooltip"),
                placement: "bottom",
                placeholder: "{{anyDependencies}}",
                extraChildren: QueryTutorials.dataResponder && (
                  <DocLink style={{ marginTop: 8 }} href={QueryTutorials.dataResponder}>
                    {trans("dataResponder.docLink")}
                  </DocLink>
                ),
              })}
            </QuerySectionWrapper>

            <QuerySectionWrapper>
              <QueryConfigWrapper>
                <QueryConfigLabel labelHeight="auto">
                  {trans("eventHandler.eventHandlers")}
                </QueryConfigLabel>
                {children.onEvent.getPropertyView()}
              </QueryConfigWrapper>
            </QuerySectionWrapper>
          </QueryPropertyViewWrapper>
        ),
      },
    ], [children.data, children.onEvent])
    return (
      <BottomTabs
        type={BottomResTypeEnum.DateResponder}
        tabsConfig={tabsConfig}
        tabTitle={children.name.getView()}
        status=""
        commentary={children.commentary.getView()}
      />
    );
  })
  .build();

class DataChangeResponderAsBottomRes extends DataResponderItemCompBase implements BottomResComp {
  result(): BottomResCompResult | null {
    return null;
  }
  type(): BottomResTypeEnum {
    return BottomResTypeEnum.DateResponder;
  }
  id(): string {
    return this.name();
  }
  name(): string {
    return this.children.name.getView();
  }
  icon(): ReactNode {
    return getBottomResIcon(BottomResTypeEnum.DateResponder);
  }
  commentary(): string {
    return this.children.commentary.getView();
  }
  order(): number {
    return this.children.order.getView();
  }
  changeCommentary(value: string) {
    this.children.commentary.dispatchChangeValueAction(value)
  }
  reduce(action: CompAction<any>) {
    if (action.type === CompActionTypes.UPDATE_NODES_V2) {
      const value = action.value.data.value;
      const dsl = this.children.data.toJsonValue();
      const lastDataValueKey = "__data_responder_data_last_value";
      const lastDataDslKey = "__data_responder_last_dsl";
      const target = this as any;
      const preValue = target[lastDataValueKey];
      const preDsl = target[lastDataDslKey];
      const valueChanged = !_.isEqual(preValue, value);
      const dslNotChanged = _.isEqual(preDsl, dsl);

      if (valueChanged && dslNotChanged) {
        const onEvent = this.children.onEvent.getView();
        setTimeout(() => {
          onEvent("dataChange");
        });
      }

      const next = super.reduce(action);
      return setFieldsNoTypeCheck(next, {
        [lastDataValueKey]: value,
        [lastDataDslKey]: dsl,
      });
    }
    return super.reduce(action);
  }
}

export const DataChangeResponderItemComp = withExposingConfigs(DataChangeResponderAsBottomRes, [
  new NameConfig("data", trans("dataResponder.dataDesc")),
]);

export const DataChangeResponderListComp = bottomResListComp(
  DataChangeResponderItemComp,
  BottomResTypeEnum.DateResponder,
  {
    data: "",
  }
);
