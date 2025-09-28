import { CompIDE } from "barda-sdk";
import { createRoot } from 'react-dom/client';
import { barda, name, version } from "./package.json";
import compMap from "./src/index";
import { GlobalInstances, antd } from "barda-sdk";
import "barda-sdk/dist/style.css";

function CompDevApp() {
  return (
    <antd.App>
      <GlobalInstances />
      <CompIDE
        compMap={compMap}
        packageName={name}
        packageVersion={version}
        compMeta={barda.comps}
      />
    </antd.App>
  );
}

const container = document.querySelector("#root");
const root = createRoot(container!);
root.render(<CompDevApp />);
