import { anchorItemKey } from "@barda/pages/editor/left/settingsPanel";
import { Card, Divider, Typography } from "antd";
import {
  clearMockWindow,
  clearStyleEval,
  ConstructorToComp,
  evalFunc,
  evalStyle,
  RecordConstructorToComp,
} from "barda-core";
import { PlusIcon, ScrollBar } from "barda-design";
import { EmptyContent } from "components/EmptyContent";
import { HelpText } from "components/HelpText";
import { UnifiedLibraryModal } from "components/UnifiedLibraryModal";
import { JSLibraryTree } from "components/JSLibraryTree";
import { CodeTextControl } from "comps/controls/codeTextControl";
import SimpleStringControl from "comps/controls/simpleStringControl";
import { MultiCompBuilder, withPropertyViewFn } from "comps/generators";
import { list } from "comps/generators/list";
import { getGlobalSettings } from "comps/utils/globalSettings";
import { trans } from "i18n";
import log from "loglevel";
import { useEffect } from "react";
import styled from "styled-components";
import { runScriptInHost } from "util/commonUtils";
import { fetchJSLibrary } from "util/jsLibraryUtils";
const { Text } = Typography;

export interface ExternalPreload {
  css?: string;
  libs?: string[];
  script?: string;
  runJavaScriptInHost?: boolean;
}

interface RunAndClearable<T> {
  run(id: string, externalPreload?: T): Promise<any>;

  clear(): Promise<any>;
}

class LibsCompBase extends list(SimpleStringControl) implements RunAndClearable<string[]> {
  success: Record<string, boolean> = {};
  globalVars: Record<string, string[]> = {};
  externalLibs: string[] = [];
  runInHost: boolean = false;

  getAllLibs() {
    return this.externalLibs.concat(this.getView().map((i) => i.getView()));
  }

  async loadScript(url: string) {
    if (this.success[url]) {
      return;
    }
    return fetchJSLibrary(url).then((code) => {
      evalFunc(
        code,
        {},
        {},
        {
          scope: "function",
          disableLimit: this.runInHost,
          onSetGlobalVars: (v: string) => {
            this.globalVars[url] = this.globalVars[url] || [];
            if (!this.globalVars[url].includes(v)) {
              this.globalVars[url].push(v);
            }
          },
        }
      );
      this.success[url] = true;
    });
  }

  async loadAllLibs() {
    const scriptRunners = this.getAllLibs().map((url) =>
      this.loadScript(url).catch((e) => {
        log.warn(e);
      })
    );

    try {
      await Promise.all(scriptRunners);
    } catch (e) {
      log.warn("load preload libs error:", e);
    }
  }

  async run(id: string, externalLibs: string[] = [], runInHost: boolean = false) {
    this.externalLibs = externalLibs;
    this.runInHost = runInHost;
    return this.loadAllLibs();
  }

  async clear(): Promise<any> {
    clearMockWindow();
  }
}

const LibsComp = withPropertyViewFn(LibsCompBase, (comp) => {
  useEffect(() => {
    comp.loadAllLibs();
  }, [comp.getView().length]);
  return (
    <ScrollBar style={{ height: "295px" }}>
      {comp.getAllLibs().length === 0 && (
        <EmptyContent text={trans("preLoad.jsLibraryEmptyContent")} />
      )}
      <JSLibraryTree
        mode={"column"}
        libs={comp
          .getView()
          .map((i) => ({
            url: i.getView(),
            deletable: true,
            exportedAs: comp.globalVars[i.getView()]?.[0],
          }))
          .concat(
            comp.externalLibs.map((l) => ({
              url: l,
              deletable: false,
              exportedAs: comp.globalVars[l]?.[0],
            }))
          )}
        onDelete={(idx) => {
          comp.dispatch(comp.deleteAction(idx));
        }}
      />
    </ScrollBar>
  );
});

function runScript(code: string, inHost?: boolean) {
  if (inHost) {
    runScriptInHost(code);
    return;
  }
  try {
    evalFunc(code, {}, {});
  } catch (e) {
    log.error(e);
  }
}

class ScriptComp extends CodeTextControl implements RunAndClearable<string> {
  runInHost: boolean = false;

  runPreloadScript() {
    const code = this.getView();
    if (!code) {
      return;
    }
    runScript(code, this.runInHost);
  }

  async run(id: string, externalScript: string = "", runInHost: boolean = false) {
    this.runInHost = runInHost;
    if (externalScript) {
      runScript(externalScript, runInHost);
    }
    this.runPreloadScript();
  }

  async clear(): Promise<any> {
    clearMockWindow();
  }
}

class CSSComp extends CodeTextControl implements RunAndClearable<string> {
  id = "";
  externalCSS: string = "";

  async applyAllCSS() {
    const css = this.getView();
    evalStyle(this.id, [this.externalCSS, css]);
  }

  async run(id: string, externalCSS: string = "") {
    this.id = id;
    this.externalCSS = externalCSS;
    return this.applyAllCSS();
  }

  async clear() {
    clearStyleEval(this.id);
  }
}

const childrenMap = {
  libs: LibsComp,
  script: ScriptComp,
  css: CSSComp,
};

type ChildrenInstance = RecordConstructorToComp<typeof childrenMap>;

function JavaScriptTabPane(props: { comp: ConstructorToComp<typeof ScriptComp> }) {
  useEffect(() => {
    props.comp.runPreloadScript();
  }, [props.comp]);

  const codePlaceholder = `window.name = 'Tom';\nwindow.greet = () => "hello world";`;

  return (
    <>
      <Divider />
      <Text id={anchorItemKey.PreLoadJS} strong>{trans("advanced.preloadJSTitle")}</Text>
      <HelpText style={{ marginBottom: 20 }}>{trans("preLoad.jsHelpText")}</HelpText>
      {props.comp.propertyView({
        styleName: "window",
        codeType: "Function",
        language: "javascript",
        placeholder: codePlaceholder,
      })}
    </>
  );
}

function CSSTabPane(props: { comp: CSSComp }) {
  useEffect(() => {
    props.comp.applyAllCSS();
  }, [props.comp]);

  const codePlaceholder = `.top-header {\n  background-color: red; \n}`;

  return (
    <>
      <Divider />
      <Text id={anchorItemKey.CSS} strong>CSS</Text>
      <HelpText style={{ marginBottom: 20 }}>{trans("preLoad.cssHelpText")}</HelpText>
      {props.comp.propertyView({
        placeholder: codePlaceholder,
        styleName: "window",
        language: "css",
      })}
    </>
  );
}

const Wrapper = styled.div`
  padding: 16px 16px 0 16px;
`;

function PreloadConfigModal(props: ChildrenInstance) {
  return (
    <Wrapper >
      <JavaScriptTabPane comp={props.script} />
      <CSSTabPane comp={props.css} />
    </Wrapper>

  );
}

const PreloadCompBase = new MultiCompBuilder(childrenMap, () => { })
  .setPropertyViewFn((children) => <PreloadConfigModal {...children} />)
  .build();

const AddJSLibraryButton = styled.div`
  cursor: pointer;
  g g {
    stroke: #8b8fa3;
  }
  &:hover {
    g g {
      stroke: #222222;
    }
  }
`;

export class PreloadComp extends PreloadCompBase {
  async clear() {
    return Promise.allSettled(Object.values(this.children).map((i) => i.clear()));
  }

  async run(id: string) {
    const { orgCommonSettings = {} } = getGlobalSettings();
    const { preloadCSS, preloadJavaScript, preloadLibs, runJavaScriptInHost } = orgCommonSettings;
    await this.children.css.run(id, preloadCSS || "");
    await this.children.libs.run(id, preloadLibs || [], !!runJavaScriptInHost);
    await this.children.script.run(id, preloadJavaScript || "", !!runJavaScriptInHost);
  }

  getJSLibraryPropertyView() {
    const libs = this.children.libs;
    return (
      <Wrapper>
        <Card
          id={anchorItemKey.Lib}
          title={trans("advanced.preloadLibsTitle")}
          extra={
            <AddJSLibraryButton>
              <UnifiedLibraryModal
                runInHost={libs.runInHost}
                trigger={<PlusIcon height={"46px"} />}
                onCheck={(url) => !libs.getAllLibs().includes(url ?? "")}
                onLoad={(url) => libs.loadScript(url)}
                onSuccess={(url) => libs.dispatch(libs.pushAction(url))}
                onDelete={(url) => {
                  const idx = libs.getView().findIndex((i: InstanceType<typeof SimpleStringControl>) => i.getView() === url);
                  if (idx !== -1) {
                    libs.dispatch(libs.deleteAction(idx));
                  }
                }}
              />
            </AddJSLibraryButton>
          }
        >
          <Card.Meta description={trans("preLoad.jsLibraryHelpText")} />
          <p />
          {this.children.libs.getPropertyView()}
        </Card>
      </Wrapper>
    );
  }
}
