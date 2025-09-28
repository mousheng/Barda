import { EvalMethods } from "../types/evalTypes";
import log from "loglevel";

// 全局变量黑名单，在 jsQuery/jsAction 中禁止使用
const functionBlacklist = new Set<PropertyKey>([
  "top",
  "parent",
  "document",
  "location",
  "chrome",
  "fetch",
  "XMLHttpRequest",
  "importScripts",
  "Navigator",
  "MutationObserver",
]);

const expressionBlacklist = new Set<PropertyKey>([
  ...Array.from(functionBlacklist.values()),
  "setTimeout",
  "setInterval",
  "setImmediate",
]);

const globalVarNames = new Set<PropertyKey>(["window", "globalThis", "self", "global"]);

/**
 * 创建一个黑洞对象，该对象在被访问时会返回自身。
 * 
 * 黑洞对象的作用是在任何属性被访问时，返回一个新的黑洞对象，
 * 避免了因为访问不存在的属性而抛出错误。这对于模拟一些
 * 应该吞噬所有交互而不产生任何效果的对象时非常有用。
 * 
 * @returns 返回一个黑洞对象，该对象在被访问时返回自身。
 */
export function createBlackHole(): any {
  // 使用 Proxy 创建黑洞对象，拦截并处理属性访问。
  return new Proxy(
    function () {
      return createBlackHole();
    },
    {
      // 当访问黑洞对象的属性时，get 陷阱会被触发。
      get(t, p, r) {
        // 如果尝试访问 toString 方法，返回一个函数，该函数返回空字符串。
        if (p === "toString") {
          return function () {
            return "";
          };
        }
        // 如果尝试访问 Symbol.toPrimitive，也返回一个函数，该函数返回空字符串。
        // 这是为了确保黑洞对象在需要原始值时的行为也是定义好的。
        if (p === Symbol.toPrimitive) {
          return function () {
            return "";
          };
        }
        // 记录日志，指示访问了黑洞对象的哪个属性，并返回一个新的黑洞对象。
        log.log(`[Sandbox] access ${String(p)} on black hole, return mock object`);
        return createBlackHole();
      },
    }
  );
}

/**
 * 创建一个模拟的全局对象
 * 
 * 此函数用于创建一个模拟的全局对象（如window），该对象在处理特定属性时会有特殊行为
 * 特别适用于测试或者沙箱环境，以拦截和模拟全局对象的行为
 * 
 * @param base 可选的基础对象，用于继承属性和方法，默认为空对象
 * @param blacklist 属性黑名单，定义了哪些属性会被限制访问
 * @param onSet 可选的回调函数，当属性设置时被调用
 * @param disableLimit 是否禁用黑名单限制，用于特定情况下允许访问黑名单中的属性
 * @returns 返回创建的模拟全局对象
 */
function createMockWindow(
  base?: object,
  blacklist: Set<PropertyKey> = expressionBlacklist,
  onSet?: (name: string) => void,
  disableLimit?: boolean
) {
  // 创建一个代理对象，用于拦截并处理对全局对象的访问
  const win: any = new Proxy(Object.assign({}, base), {
    // 拦截'has'操作，始终返回true，确保所有属性都被认为是存在的
    has() {
      return true;
    },
    // 拦截'set'操作，处理属性设置时的行为
    set(target, p, newValue) {
      // 如果属性名是字符串，调用onSet回调函数（如果提供的话）
      if (typeof p === "string") {
        onSet?.(p);
      }
      // 使用Reflect.set来设置目标对象的属性值，并返回结果
      return Reflect.set(target, p, newValue);
    },
    // 拦截'get'操作，处理属性访问时的行为
    get(target, p) {
      // 如果目标对象中已存在该属性，则直接返回
      if (p in target) {
        return Reflect.get(target, p);
      }
      // 如果请求的属性是预定义的全局变量名之一，返回模拟对象自身
      if (globalVarNames.has(p)) {
        return win;
      }
      // 如果属性在黑名单中且没有禁用限制，则创建并返回一个黑洞对象以吸收该访问
      if (typeof p === "string" && blacklist?.has(p) && !disableLimit) {
        log.log(`[Sandbox] access ${String(p)} on mock window, return mock object`);
        return createBlackHole();
      }
      // 对于其他属性，从原生的全局对象中获取并返回
      return getPropertyFromNativeWindow(p);
    },
  });
  // 返回创建的模拟全局对象
  return win;
}

let mockWindow: any;
let currentDisableLimit: boolean = false;

export function clearMockWindow() {
  mockWindow = createMockWindow();
}

export type SandboxScope = "function" | "expression";

export interface SandBoxOption {
  /**
 * 禁用所有限制，如同在宿主环境中运行
 */
  disableLimit?: boolean;

  /**
 * 该沙箱工作的范围，将使用不同的黑名单
 */
  scope?: SandboxScope;

  /**
 * 当设置全局变量到沙箱时的处理器，仅在范围为函数时被调用
 */
  onSetGlobalVars?: (name: string) => void;
}

function isDomElement(obj: any): boolean {
  return obj instanceof Element || obj instanceof HTMLCollection;
}

function getPropertyFromNativeWindow(prop: PropertyKey) {
  const ret = Reflect.get(window, prop);
  if (typeof ret === "function" && !ret.prototype) {
    return ret.bind(window);
  }
  // get DOM element by id, serializing may cause error
  if (isDomElement(ret)) {
    return undefined;
  }
  return ret;
}

/**
 * 创建一个代理沙箱环境，用于安全地运行不受信任的代码
 * 
 * @param context 用户传入的上下文环境，代理沙箱将会以此为基础创建
 * @param methods 可选参数，包含一系列允许在沙箱中使用的自定义方法
 * @param options 可选参数，用于配置沙箱的行为
 * 
 * 此函数的主要目的是提供一个隔离的环境，使得在沙箱中的代码不会对全局环境造成意外的修改或破坏
 */
function proxySandbox(context: any, methods?: EvalMethods, options?: SandBoxOption) {
  // 从选项中提取配置，默认值为禁用限制、作用域为"expression"，并应用用户提供的全局变量设置
  const { disableLimit = false, scope = "expression", onSetGlobalVars } = options || {};

  // 检查一个属性是否受保护，受保护的属性不能被修改或删除
  const isProtectedVar = (key: PropertyKey) => {
    return key in context || key in (methods || {}) || globalVarNames.has(key);
  };

  // 创建一个缓存对象用于存储对context对象属性的引用，以提高检索性能
  const cache = {};
  // 根据作用域选择相应的黑名单，函数作用域和表达式作用域有不同的限制
  const blacklist = scope === "function" ? functionBlacklist : expressionBlacklist;

  // 如果作用域为函数、mockWindow不存在或禁用限制的设置发生变化，则重新创建mockWindow
  if (scope === "function" || !mockWindow || disableLimit !== currentDisableLimit) {
    mockWindow = createMockWindow(mockWindow, blacklist, onSetGlobalVars, disableLimit);
  }
  // 更新当前的禁用限制设置
  currentDisableLimit = disableLimit;

  // 返回mockWindow的代理，用于拦截并处理对沙箱环境的访问和修改
  return new Proxy(mockWindow, {
    has(target, p) {
      // 代理所有变量，确保沙箱环境中所有属性都可以被检测到
      return true;
    },
    get(target, p, receiver) {
      // 处理属性访问，根据规则决定返回什么值
      if (p === Symbol.unscopables) {
        return undefined;
      }

      if (p === "toJSON") {
        return target;
      }

      if (globalVarNames.has(p)) {
        return target;
      }

      if (p in context) {
        if (p in cache) {
          return Reflect.get(cache, p);
        }
        let value = Reflect.get(context, p, receiver);
        if (typeof value === "object" && value !== null) {
          if (methods && p in methods) {
            value = Object.assign({}, value, Reflect.get(methods, p));
          }
          Object.freeze(value);
          Object.values(value).forEach(Object.freeze);
        }
        Reflect.set(cache, p, value);
        return value;
      }

      if (disableLimit) {
        return getPropertyFromNativeWindow(p);
      }

      return Reflect.get(target, p, receiver);
    },

    set(target, p, value, receiver) {
      // 防止修改受保护的变量
      if (isProtectedVar(p)) {
        throw new Error(p.toString() + " can't be modified");
      }
      return Reflect.set(target, p, value, receiver);
    },

    defineProperty(target, p, attributes) {
      // 防止定义新的受保护变量
      if (isProtectedVar(p)) {
        throw new Error("can't define property:" + p.toString());
      }
      return Reflect.defineProperty(target, p, attributes);
    },

    deleteProperty(target, p) {
      // 防止删除受保护的变量
      if (isProtectedVar(p)) {
        throw new Error("can't delete property:" + p.toString());
      }
      return Reflect.deleteProperty(target, p);
    },

    setPrototypeOf(target, v) {
      // 禁止修改原型链，以保持沙箱环境的隔离和稳定
      throw new Error("can't invoke setPrototypeOf");
    },
  });
}

export function evalScript(script: string, context: any, methods?: EvalMethods) {
  return evalFunc(`return (${script}\n);`, context, methods);
}

export function evalFunc(
  functionBody: string,
  context: any,
  methods?: EvalMethods,
  options?: SandBoxOption,
  isAsync?: boolean
) {
  const code = `with(this){
    return (${isAsync ? "async " : ""}function() {
      'use strict';
      ${functionBody};
    }).call(this);
  }`;

  // eslint-disable-next-line no-new-func
  const vm = new Function(code);
  const sandbox = proxySandbox(context, methods, options);
  const result = vm.call(sandbox);
  return result;
}
