import { withTypeAndChildrenAbstract } from "comps/generators/withType";
import { trans } from "i18n";
import { CompConstructor } from "barda-core";
import { Dropdown, ValueFromOption } from "barda-design";
import { buildQueryCommand, FunctionProperty, toQueryView } from "./queryCompUtils";
import { withPropertyViewFn } from "../generators";
import { ParamsStringControl } from "../controls/paramsControl";

const CommandOptions = [
  { label: trans("bardaQuery.queryOrgUsers"), value: "queryOrgUsers" },
  { label: trans("bardaQuery.queryFolderApps"), value: "queryFolderApps" },
] as const;

const folderName = {
  folderName: withPropertyViewFn(ParamsStringControl, (comp) =>
    comp.propertyView({
      label: trans('bardaQuery.folderNameLabel'),
      placement: "bottom",
      placeholder: trans('bardaQuery.folderNamePlaceHolder'),
    })
  ),
};

const CommandMap: Record<
  ValueFromOption<typeof CommandOptions>,
  CompConstructor<FunctionProperty[]>
> = {
  queryOrgUsers: buildQueryCommand({}),
  queryFolderApps: buildQueryCommand({ ...folderName }),
};

const BardaTmpQuery = withTypeAndChildrenAbstract(CommandMap, "queryOrgUsers", {});

export class BardaQuery extends BardaTmpQuery {
  override getView() {
    const params = this.children.comp.getView();
    return toQueryView(params);
  }

  override getPropertyView() {
    return (
      <>
        <Dropdown
          label={trans("query.method")}
          placement={"bottom"}
          options={CommandOptions}
          value={this.children.compType.getView()}
          onChange={(value) => this.dispatch(this.changeValueAction({ compType: value, comp: {} }))}
        />
        {this.children.comp.getPropertyView()}
      </>
    );
  }
}
