import { Route, Switch } from "react-router";
import { BRANDING_SETTING } from "constants/routesURL";
import { BrandingConfigComp } from "./brandingConfigComp";

export const BrandingSetting = () => {
    return (
        <Switch>
            <Route path={BRANDING_SETTING} component={BrandingConfigComp} exact />
        </Switch>
    );
};
