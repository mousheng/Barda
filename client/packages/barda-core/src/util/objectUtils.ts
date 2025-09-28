import _ from "lodash";
import { CACHE_PREFIX } from "./cacheUtils";
import log from "loglevel";

/**
 * 如果给定的结果与之前保存的结果相等，则返回之前保存的结果；否则保存给定的结果并返回它
 * 
 * 此函数用于缓存计算结果，特别是当计算结果取决于某个键时，如果多次计算得到相同的结果，
 * 则可以直接返回之前计算的结果，避免重复计算
 * 
 * @param target 用于存储缓存结果的对象
 * @param key 用于标识缓存结果的键值
 * @param result 计算得到的结果，如果与之前的结果不相等，则保存并返回它
 * @param isEqual 用于比较两个结果是否相等的函数如果结果相等，则直接返回缓存的结果
 * @returns 返回之前保存的结果（如果与当前结果相等）或者当前结果
 */
export function lastValueIfEqual<T>(
  target: any,
  key: string,
  result: T,
  isEqual: (a: T, b: T) => boolean
): T {
  // 构造缓存键，用于在目标对象中存储缓存结果
  const cacheKey = "__lvif__" + key;

  // 检查目标对象中是否已缓存结果，并且当前结果与缓存结果相等
  if (target[cacheKey] && isEqual(target[cacheKey], result)) {
    // 如果相等，则直接返回缓存的结果
    return target[cacheKey];
  }

  // 将当前结果保存到目标对象中，以便下次使用
  target[cacheKey] = result;

  // 返回当前结果
  return result;
}

/**
 * 为一个给定的方法添加节流或防抖功能
 * 这个函数目的是控制方法的执行频率，以提高性能，避免短时间内频繁执行
 * 
 * @param target 任意对象，通常是一个类的实例，用于访问目标方法
 * @param key 字符串，表示目标方法的名称
 * @param mode 字符串，指定限流模式，可以是 "debounce"（防抖）或 "throttle"（节流）
 * @param delay 可选的数字参数，指定延迟的时间间隔（毫秒），用于控制方法执行的间隔
 * 
 * @returns 返回一个函数，该函数是应用了节流或防抖策略后的目标方法
 */
export function limitExecutor(
  target: any,
  key: string,
  mode: "debounce" | "throttle",
  delay?: number
) {
  // 根据提供的模式选择是使用节流还是防抖，初始化相应的Lodash方法
  // 然后调用lastValueIfEqual函数，比较上次和本次的配置是否相同
  // 如果相同，则返回上次的函数；否则，创建并返回一个新的函数
  // 这样做是为了确保每次限流或防抖的配置改变时，都能重新创建对应的函数
  return lastValueIfEqual(
    target,
    key,
    {
      delay: delay,
      mode: mode,
      // 根据模式选择 _.debounce 或 _.throttle，然后立即应用
      // 这里传递一个简单的函数 (x) => x()，因为它将被lastValueIfEqual函数替换为实际的目标方法
      func: (mode === "throttle" ? _.throttle : _.debounce)((x) => x(), delay),
    },
    (a, b) => {
      // 比较两个配置对象的delay和mode属性是否相等
      // 用于判断是否需要重新创建限流或防抖的函数
      return a.delay === b.delay && a.mode === b.mode;
    }
  ).func;
}

/**
 * 判断两个对象是否具有相同的键值对。
 * 这个函数用于比较两个对象的浅层属性是否完全一致，主要用于React组件的shouldComponentUpdate生命周期方法中，
 * 以避免不必要的组件重新渲染。
 * 
 * @param obj1 第一个需要比较的对象。
 * @param obj2 第二个需要比较的对象。
 * @returns 如果两个对象的键值对完全相同，则返回true；否则，返回false。
 */
export function shallowEqual(obj1: Record<string, any>, obj2: Record<string, any>): boolean {
  // 如果两个对象引用相同，则直接返回true，因为它们是同一个对象。
  if (obj1 === obj2) {
    return true;
  }

  // 检查两个对象的键数量是否相同，并且每个键值对是否相等。
  // 使用Object.keys来确保只比较对象自身的属性，而不是原型链上的属性。
  return (
    Object.keys(obj1).length === Object.keys(obj2).length &&
    Object.keys(obj1).every((key) => obj2.hasOwnProperty(key) && obj1[key] === obj2[key])
  );
}

/**
 * 检查对象是否包含指定的字段
 * 
 * 此函数用于验证对象 `obj` 是否包含了由 `fields` 指定的所有字段，并且这些字段的值与 `fields` 中的相应值相等
 * 如果所有指定的字段都存在于 `obj` 中且值相等，则返回 `true`；否则，返回 `false`
 * 
 * @param obj - 待检查的对象，类型为 `Record<string, any>`，允许任何字符串键对应的任何类型值
 * @param fields - 可选参数，指定需要检查的字段及其值的对象，类型为 `Record<string, any>` 如果未提供（即 `undefined`），函数默认返回 `true`
 * @returns 返回一个布尔值，表示 `obj` 是否包含 `fields` 中指定的所有字段且值相等
 */
export function containFields(obj: Record<string, any>, fields?: Record<string, any>): boolean {
  // 如果未指定需要检查的字段，直接返回 true，表示默认所有字段都符合要求
  if (fields === undefined) {
    return true;
  }
  // 查找第一个不符合要求的字段索引
  const notEqualIndex = Object.keys(fields).findIndex((key) => {
    // 如果字段的值不相等，则返回对应的索引
    return obj[key] !== fields[key];
  });
  // 如果所有字段的值都相等，则 `findIndex` 返回 -1，此时返回 true；否则返回 false
  return notEqualIndex === -1;
}

/**
 * 返回一个新对象，该对象基于原对象并添加了新的字段。
 *
 * @remarks
 * 实现方式直接复制原始对象，而不是调用构造函数。如果原始对象中有绑定的函数，可能会导致错误。
 *
 * 目前 TypeScript 只支持从第二个参数获取公共字段。
 * https://stackoverflow.com/questions/57066049/list-private-property-names-of-the-class
 */
export function setFields<T>(obj: T, fields: Partial<T>) {
  return setFieldsNoTypeCheck(obj, fields);
}

/**
* 类型不安全，用户应自行保管。
* 优点：此函数可以支持私有字段。
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
 * 获取对象的类型
 * 
 * 此函数通过Object.prototype.toString.call(obj)的方式获取对象的确切类型，这种方式可以返回更详细的类型信息，比如Array、RegExp等
 * 然后，它会根据返回的类型信息，将其转换为项目中定义的类型别名如果类型在TYPES映射中存在，则使用映射中的值替换原始类型
 * 这种方式有利于统一项目中的类型表示，提高代码的可读性和一致性
 * 
 * @param obj 一个unknown类型的变量，可以是任何类型
 * @returns 返回对象的类型字符串
 */
export function toType(obj: unknown): string {
  // 匹配对象的构造函数名称，去掉前面的" [object "，得到对象类型
  let type: string = ({} as any).toString.call(obj).match(/\s([a-zA-Z]+)/)[1];

  // 如果类型在TYPES字典中存在，则使用映射后的类型
  if (TYPES.hasOwnProperty(type)) {
    type = TYPES[type];
  }

  // 返回最终确定的类型字符串
  return type;
}

/**
 * 安全地将对象转换为JSON字符串
 * 
 * 此函数尝试将给定的对象转换为JSON字符串如果转换过程中发生任何错误，它将捕获异常并记录错误日志，
 * 同时返回一个空字符串这种方法可以避免由于对象转换失败而导致的程序崩溃，使得错误处理更加优雅
 * 
 * @param obj 任何类型的对象，将被尝试转换为JSON字符串
 * @returns 如果转换成功，则返回JSON字符串；否则返回空字符串
 */
export function safeJSONStringify(obj: any): string {
  try {
    return JSON.stringify(obj);
  } catch (e) {
    log.error(e);
    return "";
  }
}

/**
 * 获取对象的唯一ID
 *
 * 此函数为每个非空对象分配一个唯一的整数ID，并缓存已分配过的对象ID。对于同一个对象，始终返回相同的ID。
 * 对于未分配过ID的新对象，会为其分配一个新的递增ID，并将其存储在WeakMap中。
 * 对于空对象或undefined，返回0。
 *
 * @param obj 需要获取ID的对象
 * @returns 对象的唯一ID，如果对象为空或undefined，则返回0
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
