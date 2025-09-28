import { Input } from "barda-design";
import { SimpleComp } from "barda-core";
import { ControlPropertyViewWrapper } from "barda-design";
import { ControlParams } from "./controlParams";

class SimpleStringControl extends SimpleComp<string> {
  readonly IGNORABLE_DEFAULT_VALUE = "";
  protected getDefaultValue(): string {
    return "";
  }

  getPropertyView() {
    return this.propertyView({});
  }

  propertyView(params: ControlParams) {
    return (
      <ControlPropertyViewWrapper {...params}>
        <Input
          value={this.value}
          placeholder={params.placeholder}
          onChange={(e) => {
            this.dispatchChangeValueAction(e.target.value);
          }}
        />
      </ControlPropertyViewWrapper>
    );
  }
}

export default SimpleStringControl;
