import { checkCursorInBinding } from "base/codeEditor/codeEditorUtils";
import { Completion, CompletionContext, CompletionResult } from "base/codeEditor/codeMirror";
import { CompletionsQuery, Def, Server } from "tern";
import ecma from "./defs/ecmascript.json";
import ecmaZh from "./defs/ecmascript-zh.json";
import lodash from "./defs/lodash.json";
import lodashZh from "./defs/lodash-zh.json";
import { CompletionSource } from "./completion";
import { language } from "@barda/i18n";

// 根据语言动态选择ecmascript定义
const getEcmaDef = (): Def => {
  return language === "zh" ? (ecmaZh as unknown as Def) : (ecma as unknown as Def);
};

// 为lodash定义动态注入URL的函数
const injectLodashUrls = (def: any, isZh: boolean): any => {
  const result = { ...def };
  
  if (result._ && typeof result._ === 'object') {
    const lodashFunctions = { ...result._ };
    const baseUrl = isZh ? 'https://www.lodashjs.com/docs/lodash.' : 'https://lodash.com/docs/4.17.15#';
    
    for (const [functionName, functionDef] of Object.entries(lodashFunctions)) {
      if (typeof functionDef === 'object' && functionDef !== null) {
        lodashFunctions[functionName] = {
          ...functionDef,
          '!url': `${baseUrl}${functionName}`
        };
      }
    }
    
    result._ = lodashFunctions;
  }
  
  return result;
};

// 根据语言动态选择lodash定义并注入URL
const getLodashDef = (): Def => {
  const isZh = language === "zh";
  const baseDef = isZh ? lodashZh : lodash;
  const defWithUrls = injectLodashUrls(baseDef, isZh);
  return defWithUrls as unknown as Def;
};

export enum AutocompleteDataType {
  OBJECT = "Object",
  NUMBER = "Number",
  ARRAY = "Array",
  FUNCTION = "Function",
  BOOLEAN = "Boolean",
  STRING = "String",
  UNKNOWN = "Unknown",
}

export function getDataType(type: string): AutocompleteDataType {
  if (type === "?") return AutocompleteDataType.UNKNOWN;
  else if (type === "number") return AutocompleteDataType.NUMBER;
  else if (type === "string") return AutocompleteDataType.STRING;
  else if (type === "bool") return AutocompleteDataType.BOOLEAN;
  else if (type === "array") return AutocompleteDataType.ARRAY;
  else if (/^fn\(/.test(type)) return AutocompleteDataType.FUNCTION;
  else if (/^\[/.test(type)) return AutocompleteDataType.ARRAY;
  else return AutocompleteDataType.OBJECT;
}

// 创建一个函数来获取Server实例，确保使用最新的语言设置
const getServer = (): Server => {
  const currentDefs: Def[] = [
    // @ts-ignore
    getEcmaDef(),
    // @ts-ignore
    getLodashDef(),
  ];
  return new Server({ defs: currentDefs });
};

export class TernServer extends CompletionSource {
  // 添加上下文信息属性
  private exposingData?: Record<string, unknown>;
  private queryContext?: any;

  // 配置项
  private readonly config = {
    maxArraySize: 10,           // 最大数组元素数量
    maxObjectDepth: 3,          // 最大对象嵌套深度
    maxStringLength: 100,       // 最大字符串长度
    maxObjectProperties: 50,    // 最大对象属性数量
  };

  // 设置上下文信息的方法
  setContext(exposingData?: Record<string, unknown>, queryContext?: any) {
    this.exposingData = exposingData;
    this.queryContext = queryContext;
  }

  // 配置序列化限制参数
  setConfig(newConfig: Partial<typeof this.config>) {
    Object.assign(this.config, newConfig);
  }

  // 截断大数据并序列化，避免大对象/数组导致卡顿
  private truncateAndStringify(value: unknown, depth: number = 0): string {
    if (depth > this.config.maxObjectDepth) {
      return '{}'; // 超过最大深度，返回空对象
    }

    if (value === null || value === undefined) {
      return String(value);
    }

    const type = typeof value;

    if (type === 'string') {
      const str = value as string;
      return JSON.stringify(str.length > this.config.maxStringLength
        ? str.substring(0, this.config.maxStringLength) + '...'
        : str
      );
    }

    if (type === 'number' || type === 'boolean') {
      return JSON.stringify(value);
    }

    if (type === 'function') {
      return 'function() {}';
    }

    if (Array.isArray(value)) {
      const arr = value as unknown[];
      if (arr.length === 0) return '[]';

      const truncatedLength = Math.min(arr.length, this.config.maxArraySize);
      const truncatedArray = arr.slice(0, truncatedLength).map(item =>
        typeof item === 'object' && item !== null
          ? this.truncateAndStringify(item, depth + 1)
          : this.truncateAndStringify(item, depth)
      );

      return `[${truncatedArray.join(', ')}]`;
    }

    if (type === 'object') {
      const obj = value as Record<string, unknown>;
      const entries = Object.entries(obj);

      if (entries.length === 0) return '{}';

      const truncatedEntries = entries.slice(0, this.config.maxObjectProperties);
      const objPairs = truncatedEntries.map(([key, val]) => {
        const safeKey = JSON.stringify(key);
        const safeVal = this.truncateAndStringify(val, depth + 1);
        return `${safeKey}: ${safeVal}`;
      });

      return `{${objPairs.join(', ')}}`;
    }

    return '{}';
  }

  // 生成包含上下文信息的JavaScript代码
  private generateContextCode(): string {
    let contextCode = "";

    // 添加上下文数据
    if (this.exposingData) {
      contextCode += "// Context data for better type inference\n";
      Object.entries(this.exposingData).forEach(([key, value]) => {
        // 添加JSDoc注释以提供更好的类型信息
        const typeInfo = this.getTypeInfo(value);
        contextCode += `/**\n * @type {${typeInfo}}\n */\n`;

        // 使用截断序列化方法
        const safeValue = this.truncateAndStringify(value);
        contextCode += `var ${key} = ${safeValue};\n`;

        // 为数组添加长度信息
        if (Array.isArray(value) && value.length > this.config.maxArraySize) {
          contextCode += `// Original array length: ${value.length}\n`;
          contextCode += `${key}.length = ${value.length};\n`;
        }

        // 为对象添加属性类型提示（仅限于小对象）
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
          const entries = Object.entries(value);
          if (entries.length <= this.config.maxObjectProperties) {
            entries.forEach(([propKey, propValue]) => {
              const propType = this.getTypeInfo(propValue);
              contextCode += `/**\n * @type {${propType}}\n */\n`;
              contextCode += `${key}.${propKey};\n`;
            });
          }
        }
      });
    }

    // 添加查询上下文信息
    if (this.queryContext) {
      contextCode += "// Query context information\n";
      if (this.queryContext.datasourceId) {
        contextCode += `/**\n * @type {string}\n */\n`;
        contextCode += `var datasourceId = "${this.queryContext.datasourceId}";\n`;
      }
      if (this.queryContext.resourceType) {
        contextCode += `/**\n * @type {string}\n */\n`;
        contextCode += `var resourceType = "${this.queryContext.resourceType}";\n`;
      }
    }

    return contextCode;
  }

  // 获取值的类型信息
  private getTypeInfo(value: unknown): string {
    if (value === null) return "null";
    if (value === undefined) return "undefined";

    const type = typeof value;
    switch (type) {
      case 'string':
        return "string";
      case 'number':
        return "number";
      case 'boolean':
        return "boolean";
      case 'function':
        return "Function";
      case 'object':
        if (Array.isArray(value)) {
          if (value.length === 0) return "Array";
          const firstType = this.getTypeInfo(value[0]);
          return `${firstType}[]`;
        }
        return "Object";
      default:
        return "any";
    }
  }

  completionSource(
    context: CompletionContext
  ): CompletionResult | Promise<CompletionResult | null> | null {
    // log.log("complete pos:", context.pos, "\nselection:", context.state);
    const isCursorInBinding = checkCursorInBinding(context, this.isFunction);
    if (!isCursorInBinding) {
      return null;
    }
    if (
      context.matchBefore(/[A-Za-z_$][\w$]*(?:\[\s*[0-9]+\s*\])*\.?/) === null &&
      (this.isFunction || context.matchBefore(/\{\{\s*/) === null)
    ) {
      return null;
    }
    const state = context.state;
    const pos = context.pos;

    // 生成包含上下文信息的完整代码
    const contextCode = this.generateContextCode();
    const fullCode = contextCode + state.sliceDoc();
    // console.log("fullCode: ", fullCode);

    const query: CompletionsQuery = {
      type: "completions",
      types: true,
      docs: true,
      urls: true,
      origins: true,
      caseInsensitive: true,
      guess: false,
      inLiteral: false,
      includeKeywords: true,
      end: pos + contextCode.length, // 调整位置以考虑上下文代码
      file: "#0",
    };
    const files = [
      {
        type: "full",
        name: "_temp",
        text: fullCode, // 使用包含上下文信息的完整代码
      },
    ];

    const request = { query, files };
    let error;
    let data: any;
    const server = getServer(); // 使用动态获取的server实例
    server.request(request as any, (rError, rData) => {
      error = rError;
      data = rData;
    });
    // log.log("ternComplete. error:", error, "\ndata: ", data);
    if (error || data.completions.length === 0) {
      return null;
    }
    const options = [];
    for (const completion of data.completions) {
      // log.log("completion: ", completion);
      const dataType = getDataType(completion.type);
      const completionOption: Completion = {
        type: dataType, // icon
        label: completion.name,
        detail: dataType, // short message after label
        // apply,
        // info to add: completion.name, completion.url, completion.type, completion.doc
        info:
          completion.doc === undefined
            ? undefined
            : (complete: Completion) => {
              let dom = document.createElement("div");
              const urlToOpen = completion.url;
              const handleLinkClick = (event: Event) => {
                event.preventDefault();
                event.stopPropagation();

                if (!urlToOpen) return;

                try {
                  const newWindow = window.open(urlToOpen, "_blank", "noopener,noreferrer");
                  newWindow?.focus();
                } catch (error) {
                }
              };
              const hintDiv = document.createElement('div');
              hintDiv.className = 'hintDiv';
              hintDiv.style.cssText = 'cursor: pointer; user-select: none;';

              // 只有存在URL时才添加点击事件监听器
              if (urlToOpen) {
                hintDiv.addEventListener('click', handleLinkClick);
                hintDiv.addEventListener('mousedown', handleLinkClick);
                hintDiv.addEventListener('touchend', handleLinkClick);
              }

              hintDiv.innerHTML = `
                  <svg width="16px" height="16px" class="hintSvg" viewBox="0 0 16 16" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                    <g stroke="none" stroke-width="1" fill="none" fill-rule="evenodd" stroke-linecap="round">
                        <g transform="translate(8.000000, 8.000000) rotate(30.000000) translate(-8.000000, -8.000000) translate(5.000000, 1.500000)" stroke="#4965F2" stroke-width="1.5">
                            <path d="M0,4.5 L0,3 C0,1.34314575 1.34314575,0 3,0 C4.65685425,0 6,1.34314575 6,3 L6,4.5 L6,4.5 M6,8.5 L6,10 C6,11.6568542 4.65685425,13 3,13 C1.34314575,13 0,11.6568542 0,10 L0,8.5 L0,8.5"></path>
                            <line x1="3" y1="4" x2="3" y2="9"></line>
                        </g>
                    </g>
                  </svg>
                  <span class="hintName">${completion.name}</span>
                `;

              const typeSpan = document.createElement('span');
              typeSpan.className = 'hintType';
              typeSpan.textContent = completion.type;

              const docSpan = document.createElement('span');
              docSpan.className = 'hintDoc';
              docSpan.textContent = completion.doc;

              dom.appendChild(hintDiv);
              dom.appendChild(typeSpan);
              dom.appendChild(docSpan);

              return dom;
            },
        boost: -1,
      };
      options.push(completionOption);
    }

    const completions = {
      from: data.start - contextCode.length, // 调整起始位置
      validFor: /^\w*$/,
      options,
    };
    // const token = context.state.sliceDoc(completions.from, context.pos);
    // const testFlag = completions.span.test(token)
    // log.log("Tern completeContext: ", context, "\ncompletionResult: ", completions, `\ntoken: ${token}, testFlag: ${testFlag}`);
    return completions;
  }
}
