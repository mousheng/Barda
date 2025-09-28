import { createRoot } from "react-dom/client";
import { CompIDE } from "barda-sdk";
import { name, version, barda } from "./package.json";
import compMap from "./src/index";

import "barda-sdk/dist/style.css";

function CompDevApp() {
  return (
    <CompIDE
      compMap={compMap}
      packageName={name}
      packageVersion={version}
      compMeta={barda.comps}
    />
  );
}

const container = document.querySelector("#root");
const root = createRoot(container!);
root.render(<CompDevApp />);
