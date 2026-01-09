import { withPropertyViewFn, withTypeAndChildren } from "comps/generators";
import { includes } from "lodash";
import { CompAction, CompConstructor } from "barda-core";
import { Dropdown, ValueFromOption } from "barda-design";
import { dropdownControl } from "../controls/dropdownControl";
import { ParamsJsonControl, ParamsPositiveNumberControl, ParamsStringControl, ValueFunction } from "../controls/paramsControl";
import { buildQueryCommand, FunctionProperty, toQueryView } from "./queryCompUtils";
import { trans } from "@barda/i18n";

const CommandOptions = [
  { label: trans("query.findDocs"), value: "FIND" },
  { label: trans("query.insertDocs"), value: "INSERT" },
  { label: trans("query.updateDocs"), value: "UPDATE" },
  { label: trans("query.deleteDocs"), value: "DELETE" },
  { label: trans("query.count"), value: "COUNT" },
  { label: trans("query.distinct"), value: "DISTINCT" },
  { label: trans("query.aggregate"), value: "AGGREGATE" },
  { label: trans("query.raw"), value: "RAW" },
] as const;

const LimitOptions = [
  {
    label: trans("query.sigleDoc"),
    value: "SINGLE",
  },
  {
    label: trans("query.allDocs"),
    value: "ALL",
  },
] as const;

const UpsertOptions = [
  {
    label: "false",
    value: "false",
  },
  {
    label: "true",
    value: "true",
  },
] as const;

const QueryField = withPropertyViewFn(ParamsJsonControl, (comp) =>
  comp.propertyView({
    label: trans("query.query"),
    placement: "bottom",
    placeholder: `{
  rating : {$gte : 9}
}`,
    styleName: "medium",
    enableMetaCompletion: true,
  })
);

const LimitInputField = withPropertyViewFn(ParamsPositiveNumberControl, (comp) =>
  comp.propertyView({
    label: trans("query.limit"),
    placement: "bottom",
    placeholder: "10",
  })
);

const LimitDropdownField = withPropertyViewFn(dropdownControl(LimitOptions, "SINGLE"), (comp) => (
  <>{comp.propertyView({ label: trans("query.limit"), placement: "bottom" })}</>
));

const CommandMap: Record<
  ValueFromOption<typeof CommandOptions>,
  CompConstructor<FunctionProperty[]>
> = {
  FIND: buildQueryCommand({
    query: QueryField,
    projection: withPropertyViewFn(ParamsJsonControl, (comp) =>
      comp.propertyView({
        label: trans("query.projection"),
        tooltip: trans("query.projectionDes"),
        placement: "bottom",
        placeholder: "{name : 1}",
        enableMetaCompletion: true,
      })
    ),
    limit: LimitInputField,
    skip: withPropertyViewFn(ParamsPositiveNumberControl, (comp) =>
      comp.propertyView({
        label: trans("query.skip"),
        placement: "bottom",
        placeholder: "0",
      })
    ),
    sort: withPropertyViewFn(ParamsJsonControl, (comp) =>
      comp.propertyView({
        label: trans("query.sort"),
        tooltip: trans("query.sortDes"),
        placement: "bottom",
        placeholder: "{name : 1}",
        enableMetaCompletion: true,
      })
    ),
  }),
  INSERT: buildQueryCommand({
    documents: withPropertyViewFn(ParamsJsonControl, (comp) =>
      comp.propertyView({
        label: trans("query.documents"),
        placement: "bottom",
        placeholder: `[{ 
    _id: 1, 
    user: "abc123", 
}]`,
        styleName: "medium",
        enableMetaCompletion: true,
      })
    ),
  }),
  UPDATE: buildQueryCommand({
    query: QueryField,
    update: withPropertyViewFn(ParamsJsonControl, (comp) =>
      comp.propertyView({
        label: trans("query.update"),
        placement: "bottom",
        placeholder: `{
  $inc : {score: 1}
}`,
        styleName: "medium",
        enableMetaCompletion: true,
      })
    ),
    limit: LimitDropdownField,
    upsert: withPropertyViewFn(dropdownControl(UpsertOptions, "false"), (comp) => (
      <>{comp.propertyView({label: trans("query.upsert"), tooltip: trans("query.upsertDes"), placement: "bottom" })}</>
    )),
  }),
  DELETE: buildQueryCommand({
    query: QueryField,
    limit: LimitDropdownField,
  }),
  COUNT: buildQueryCommand({
    query: QueryField,
  }),
  DISTINCT: buildQueryCommand({
    query: QueryField,
    key: withPropertyViewFn(ParamsStringControl, (comp) =>
      comp.propertyView({
        label: "Key",
        placement: "bottom",
        placeholder: "name",
        enableMetaCompletion: true,
      })
    ),
  }),
  AGGREGATE: buildQueryCommand({
    arrayPipelines: withPropertyViewFn(ParamsJsonControl, (comp) =>
      comp.propertyView({
        label: trans("query.arrayPipelines"),
        placement: "bottom",
        placeholder: `[
  { $match: { gender: "male" } },
  { $group: { _id: "$team", count: { $sum: 1 } } }
]`,
        styleName: "medium",
        enableMetaCompletion: true,
      })
    ),
    limit: LimitInputField,
  }),
  RAW: buildQueryCommand({
    command: withPropertyViewFn(ParamsJsonControl, (comp) =>
      comp.propertyView({
        label: trans("query.command"),
        placement: "bottom",
        placeholder: `[
  { $project: { tags: 1 } }, { $unwind: "$tags" }, 
  { $group: { _id: "$tags", count: { $sum : 1 }}}
]`,
        styleName: "medium",
        enableMetaCompletion: true,
      })
    ),
  }),
};

const MongoQueryTmp = withTypeAndChildren(CommandMap, "FIND", {
  collection: withPropertyViewFn(ParamsStringControl, (comp) =>
    comp.propertyView({ placeholder: "users" })
  ),
});

export class MongoQuery extends MongoQueryTmp {
  isWrite(action: CompAction): boolean {
    return (
      "value" in action && includes(["INSERT", "UPDATE", "DELETE", "RAW"], action.value["compType"])
    );
  }

  override getView() {
    const params = [
      ...Object.entries(this.children.collection.getView()).map((kv) => ({
        key: kv[0],
        value: kv[1] as ValueFunction,
      })),
      ...this.children.comp.getView(),
    ];
    return toQueryView(params);
  }

  override getPropertyView() {
    return (
      <>
        <Dropdown
          label={trans("query.Commands")}
          placement={"bottom"}
          options={CommandOptions}
          value={this.children.compType.getView()}
          onChange={(value) => this.dispatch(this.changeValueAction({ compType: value, comp: {} }))}
        />

        {this.children.compType.getView() !== "RAW" && (
          <>
            {this.children.collection.propertyView({
              label: trans("query.collection"),
              placement: "bottom",
              enableMetaCompletion: true,
            })}
          </>
        )}

        {this.children.comp.getPropertyView()}
      </>
    );
  }
}
