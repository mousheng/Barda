import { CompTree } from "@barda/comps/comps/containerBase";
import { UICompType } from "@barda/comps/uiCompRegistry";
import JSONbig from "json-bigint";
import _ from "lodash";
import log from "loglevel";
import { CACHE_PREFIX } from "./cacheUtils";

export const BigJSONNative = JSONbig({ useNativeBigInt: true });
export type NodeItem = {
  key: string;
  title: string;
  type?: UICompType;
  children: NodeItem[];
};
export type NodeInfo = {
  key: string;
  show: boolean;
  clientX?: number;
};

/**
 * 如果结果等于上一个结果，则返回缓存的值。
 * 这是为了保持引用不随相等结果而改变。
 *
 * @param target 缓存将存储在其中的目标对象
 * @param key 缓存将与之关联的键
 * @param result 当前的结果值
 * @param isEqual 自定义的 isEqual 函数，用于判断两个值是否相等
 * @returns 结果或者等于的缓存值
 */
export function lastValueIfEqual<T>(
  target: Record<string, any>,
  key: string,
  result: T,
  isEqual: (a: T, b: T) => boolean
): T {
  const cacheKey = "__lvif__" + key;
  if (target[cacheKey] && isEqual(target[cacheKey], result)) {
    return target[cacheKey];
  }
  target[cacheKey] = result;
  return result;
}

/**
 * 返回一个执行器用于实现防抖或节流功能
 * 1. 计数器将存储在目标对象中
 * 2. 模式或延迟时间的更改将导致防抖和节流重新计数
 *
 * @param target 存储缓存的对象
 * @param key 缓存的键
 * @param mode 执行器的模式，可以是 "debounce" 或 "throttle"
 * @param delay 等待时间
 */

export function limitExecutor(
  target: any,
  key: string,
  mode: "debounce" | "throttle",
  delay?: number
) {
  return lastValueIfEqual(
    target,
    key,
    {
      delay: delay,
      mode: mode,
      func:
        mode === "throttle"
          ? _.throttle((x) => x(), delay, { trailing: false })
          : _.debounce((x) => x(), delay),
    },
    (a, b) => {
      return a.delay === b.delay && a.mode === b.mode;
    }
  ).func;
}

/**
 * 浅比较两个对象是否相等
 */
export function shallowEqual(obj1: Record<string, any>, obj2: Record<string, any>): boolean {
  if (obj1 === obj2) {
    return true;
  }
  return (
    Object.keys(obj1).length === Object.keys(obj2).length &&
    Object.keys(obj1).every((key) => obj2.hasOwnProperty(key) && obj1[key] === obj2[key])
  );
}

export function containAllFields(obj: Record<string, any>, fields?: Record<string, any>): boolean {
  if (fields === undefined) {
    return true;
  }
  const notEqualIndex = Object.keys(fields).findIndex((key) => {
    return obj[key] !== fields[key];
  });
  return notEqualIndex === -1;
}

// 深比较函数，且剔除指定的属性
export function depthEqualExcludingProperties(a: any, b: any, omitList: string[] = []) {
  if (a === b) {
    return true;
  }
  
  if (!a || !b || typeof a !== 'object' || typeof b !== 'object') {
    return a === b;
  }
  
  if (omitList.length === 0) {
    return _.isEqual(a, b);
  }
  
  const keysA = Object.keys(a);
  const keysB = Object.keys(b);
  
  const filteredKeysA = keysA.filter(key => !omitList.includes(key));
  const filteredKeysB = keysB.filter(key => !omitList.includes(key));
  
  if (filteredKeysA.length !== filteredKeysB.length) {
    return false;
  }
  
  for (const key of filteredKeysA) {
    if (!_.isEqual(a[key], b[key])) {
      return false;
    }
  }
  
  return true;
}

/**
 * 深度比较两个对象是否相等
 * @param o1 对象1
 * @param o2 对象2
 * @param depth 比较的深度，当 depth === 1 时，表示只比较第一层对象；当 depth === Infinity 时，表示递归比较所有层级的对象
 * @returns 在指定深度内，o1 是否等于 o2
 */
export function depthEqual(o1: any, o2: any, depth: number = Infinity): boolean {
  if (depth === 0) {
    return false;
  }
  if (o1 === o2) {
    return true;
  }
  if (!_.isObjectLike(o1) || !_.isObjectLike(o2)) {
    return false;
  }
  const size1 = _.size(o1), size2 = _.size(o2);
  if (size1 !== size2) {
    return false;
  }
  return _.every(o1, (v1, key) => depthEqual(v1, o2[key], depth - 1));
}
/**
 * 递归对象A中所有属性，如果对象B中包含相同属性则修改对象A中属性的值
 * @param obj1 目标对象
 * @param obj2 源对象
 * @param path 路径
 * @param depth 深度
 * @returns 返回修改后的对象
 */
export function depthAssignObject(obj1: any, obj2: any, path = '', depth = 3) {
  if (depth === 0) return obj1
  for (let key in obj1) {
    let currentPath = path ? `${path}.${key}` : key;
    if (typeof obj1[key] === 'object') {
      depthAssignObject(obj1[key], _.get(obj2, key), currentPath, depth - 1);
    }
    else {
      let value1 = _.get(obj1, key);
      let value2 = _.get(obj2, key);
      if (!_.isUndefined(value1) && !_.isUndefined(value2)) {
        _.set(obj1, key, value2)
      }
    }
  }
  return obj1;
}

/**
 *基于添加了字段的obj返回一个新对象。
 *
 *@备注
 *该实现在不调用构造函数的情况下直接复制原始对象。
 *如果原始对象有一些带有“bind”的函数，就会出现错误。
 */
export function setFields<T>(obj: T, fields: Partial<T>) {
  return setFieldsNoTypeCheck(obj, fields);
}

/**
 * 类型不安全，用户应自行保持安全。
 * 优点：此功能可以支持私有字段。
 */
export function setFieldsNoTypeCheck<T>(
  obj: T,
  fields: Record<string, any>,
  params?: { keepCacheKeys?: string[] }
) {
  const res = Object.assign(Object.create(Object.getPrototypeOf(obj)), obj);
  Object.keys(res).forEach((key) => {
    if (key.startsWith(CACHE_PREFIX)) {
      const propertyKey = key.slice(CACHE_PREFIX.length);
      if (!params?.keepCacheKeys || !params?.keepCacheKeys.includes(propertyKey)) {
        delete res[key];
      }
    }
  });
  return Object.assign(res, fields) as T;
}

const TYPES: Record<string, string> = {
  Number: "number",
  Boolean: "boolean",
  String: "string",
  Object: "object",
};

/**
 * 获取对象类型
 */
export function toType(obj: unknown): string {
  let type: string = ({} as any).toString.call(obj).match(/\s([a-zA-Z]+)/)[1];
  if (TYPES.hasOwnProperty(type)) {
    type = TYPES[type];
  }
  return type;
}

/**
 * 安全的对象转换字符串函数
 * @param obj 对象
 * @returns 
 */
export function safeJSONStringify(obj: any): string {
  if (typeof obj === 'string') {
    return obj;
  }
  try {
    return JSON.stringify(obj, (_, v) => typeof v === 'bigint' ? v.toString() : v);
  } catch (e) {
    log.error("序列化失败:" + e);
    return "";
  }
}

/**
 * 递归查找对象中的key
 * @param item 对象
 * @param key 查找的key
 * @returns 
 */
const find = (item: any, key: string): any => {
  if (item.key === key) {
    return item;
  }
  let v;
  if (item.children.length) {
    for (const child of item.children) {
      v = find(child, key);
      if (v) {
        return v;
      }
    }
  }
  return null;
};
/**
 * 在给定的树形结构中查找具有特定键值的节点，并返回该节点
 * @param tree 树形结构
 * @param key 查找的键
 * @returns 找到的节点
 */
export function getTreeNodeByKey(tree: any, key: string): any {
  let v;
  for (const node of tree) {
    v = find(node, key);
    if (v) {
      return v;
    }
  }
  return null;
}
/**
 * 递归遍历树形结构，生成节点列表
 * @param tree 树形结构
 * @param result 结果列表
 * @param key 父节点key
 * @returns 节点列表
 */
export const getComponentTree = (tree: CompTree, result: NodeItem[], key?: string) => {
  const { items, children } = tree;
  if (Object.keys(items).length) {
    for (const i in items) {
      const info = {
        title: items[i].children.name.getView(),
        type: items[i].children.compType.getView() as UICompType,
        key: i,
        children: [],
      };
      if (key) {
        const parent = getTreeNodeByKey(result, key);
        parent?.children.push(info);
      } else {
        result.push(info);
      }
    }
    // 根据组件的名称和序号进行排序
    result = _.sortBy(result, [(x) => BigInt(_.map(x.title, (i) => i.charCodeAt(0).toString()).join(''))]);
  }
  if (Object.keys(children).length) {
    for (const i in children) {
      getComponentTree(children[i], result, i);
    }
  }
  return result;
};

/**
 * 递归查找父节点
 * @param item 
 * @param key 
 * @param result 
 * @returns 
 */
const findParent = (item: any, key: string, result: string[]): boolean => {
  if (item.key === key) {
    return true;
  }
  let v;
  if (item.children.length) {
    for (const child of item.children) {
      v = findParent(child, key, result);
      if (v) {
        result.push(item.key);
        return true;
      }
    }
  }
  return false;
};
/**
 * 根据键值查找父节点
 * @param tree 
 * @param key 
 * @returns 
 */
export function getParentNodeKeysByKey(tree: any, key: string): string[] {
  let v;
  const result: string[] = [];
  for (const node of tree) {
    v = findParent(node, key, result);
    if (v) {
      return result;
    }
  }
  return result;
}
/**
 * 生成对象的唯一ID
 */
export const getObjectId = (function () {
  let objectCurrentId = 0;
  const objectMap = new WeakMap();
  return (obj: object | undefined) => {
    if (_.isNil(obj)) return 0;
    if (objectMap.has(obj)) {
      return objectMap.get(obj);
    }
    const id = ++objectCurrentId;
    objectMap.set(obj, id);
    return id;
  };
})();

// 采用递归来深层比较，速度很慢，仅测试用
// 防止嵌套回环
export function MyCompareObjectsTEST(obj1: any, obj2: any, compFun = false, path = '', result = {}, depth = 3) {
  if (depth === 0) return result
  for (let key in obj1) {
    let currentPath = path ? `${path}.${key}` : key;
    if (typeof obj1[key] === 'object') {
      MyCompareObjectsTEST(obj1[key], _.get(obj2, key), compFun, currentPath, result, depth - 1);
    } else if (_.isFunction(obj1[key])) {
      // 简单比较
      if (compFun && !_.get(obj1, key).toString() == _.get(obj2, key).toString()) {
        // 完全比较 
        // if (compFun && !compareFuntion(_.get(obj1, key), _.get(obj2, key))) {
        return _.set(result, currentPath, {
          obj1: _.get(obj1, key),
          obj2: _.get(obj2, key)
        });
      }
    }
    else {// 其他类型
      let value1 = _.get(obj1, key);
      let value2 = _.get(obj2, key);
      if (!_.isEqual(value1, value2)) {
        _.set(result, currentPath, {
          obj1: value1,
          obj2: value2
        });
      }
    }
  }
  return result;
}
// 测试时用于打印对象中类型为函数的key
export function printObjectFunctionKeysTEST(obj: any) {
  let x = []
  for (let value in obj) {
    if (typeof _.get(obj, value) === "function") {
      x.push(value)
    }
  }
  console.log(x)
  return x
}
