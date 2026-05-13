import { executeQueryAction, routeByNameAction } from "barda-core";
import { InputTypeEnum } from "comps/comps/moduleContainerComp/ioComp/inputListItemComp";
import { SimpleNameComp } from "comps/comps/simpleNameComp";
import { EditorStateView } from "comps/editorCompat";
import type { EditorStateCompat } from "comps/editorCompat";
import type { EditorState } from "comps/editorState";
import { MultiCompBuilder } from "comps/generators/multi";
import { BranchDiv, Dropdown } from "barda-design";
import { BottomResTypeEnum } from "types/bottomRes";
import { getPromiseAfterDispatch } from "util/promiseUtils";
import { trans } from "i18n";

const ExecuteQueryTmpAction = (function () {
  const childrenMap = {
    queryName: SimpleNameComp,
  };
  return new MultiCompBuilder(childrenMap, () => {
    return () => Promise.resolve(undefined as unknown);
  })
    .setPropertyViewFn(() => <></>)
    .build();
})();

export class ExecuteQueryAction extends ExecuteQueryTmpAction {
  override getView() {
    const queryName = this.children.queryName.getView();
    if (!queryName) {
      return () => Promise.resolve();
    }
    return () =>
      getPromiseAfterDispatch(
        this.dispatch,
        routeByNameAction(
          queryName,
          executeQueryAction({
            // can add context in the future
          })
        ),
        { notHandledError: trans("eventHandler.notHandledError") }
      );
  }

  displayName() {
    const queryName = this.children.queryName.getView();
    if (queryName) {
      return `${queryName}.run()`;
    }
  }

  propertyView({ placement }: { placement?: "query" | "table" }) {
    const getQueryOptions = (editorState?: EditorStateCompat) => {
      const options: { label: string; value: string }[] =
        editorState
          ?.queryCompInfoList()
          .map((info) => ({
            label: info.name,
            value: info.name,
          }))
          .filter(
            // Filter out the current query under query
            (option) => {
              if (
                placement === "query" &&
                editorState.selectedBottomResType === BottomResTypeEnum.Query
              ) {
                return option.value !== editorState.selectedBottomResName;
              }
              return true;
            }
          ) || [];

      // input queries
      editorState
        ?.getModuleLayoutComp()
        ?.getInputs()
        .forEach((i) => {
          const { name, type } = i.getView();
          if (type === InputTypeEnum.Query) {
            options.push({ label: name, value: name });
          }
        });
      return options;
    };
    return (
      <BranchDiv $type={"inline"}>
        <EditorStateView>
          {(editorState) => (
            <>
              <Dropdown
                showSearch={true}
                value={this.children.queryName.getView()}
                options={getQueryOptions(editorState)}
                label={trans("eventHandler.selectQuery")}
                onChange={(value) => this.dispatchChangeValueAction({ queryName: value })}
              />
            </>
          )}
        </EditorStateView>
      </BranchDiv>
    );
  }
}
