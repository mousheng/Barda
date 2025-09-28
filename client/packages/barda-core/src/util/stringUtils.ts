// process CJK character
import { JSONValue } from "util/jsonTypes";

const CHINESE_PATTERN = /[\u3040-\u30ff\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff66-\uff9f]/;

export const DEFAULT_IMG_URL =
  "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHN2ZyB3aWR0aD0iMjAwcHgiIGhlaWdodD0iMjAwcHgiIHZpZXdCb3g9IjAgMCAyMDAgMjAwIiB2ZXJzaW9uPSIxLjEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiPgogICAgPGRlZnM+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSIwLjQ1ODA2MzM3NyUiIHkxPSIwLjQ1NTg2MjA4OCUiIHgyPSI5Ni4zNzkyMTM4JSIgeTI9Ijk2LjMzNjEwNDclIiBpZD0ibGluZWFyR3JhZGllbnQtMSI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEMEQ2RTIiIG9mZnNldD0iMCUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0E4QjNDOSIgb2Zmc2V0PSIxMDAlIj48L3N0b3A+CiAgICAgICAgPC9saW5lYXJHcmFkaWVudD4KICAgIDwvZGVmcz4KICAgIDxnIGlkPSLlm77niYfnu4Tku7bpu5jorqTlm74iIHN0cm9rZT0ibm9uZSIgc3Ryb2tlLXdpZHRoPSIxIiBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPgogICAgICAgIDxnIGlkPSLnvJbnu4QtNSI+CiAgICAgICAgICAgIDxyZWN0IGlkPSLnn6nlvaIiIGZpbGw9InVybCgjbGluZWFyR3JhZGllbnQtMSkiIHg9IjAiIHk9IjAiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiByeD0iNCI+PC9yZWN0PgogICAgICAgICAgICA8ZyBpZD0i5Zu+54mHIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSg3OC4wMDAwMDAsIDgwLjAwMDAwMCkiIGZpbGw9IiNGRkZGRkYiPgogICAgICAgICAgICAgICAgPHBhdGggZD0iTTQxLDAgQzQyLjY1Njg1NDIsLTMuMDQzNTkxODhlLTE2IDQ0LDEuMzQzMTQ1NzUgNDQsMyBMNDQsMzcgQzQ0LDM4LjY1Njg1NDIgNDIuNjU2ODU0Miw0MCA0MSw0MCBMMyw0MCBDMS4zNDMxNDU3NSw0MCAyLjAyOTA2MTI1ZS0xNiwzOC42NTY4NTQyIDAsMzcgTDAsMyBDLTIuMDI5MDYxMjVlLTE2LDEuMzQzMTQ1NzUgMS4zNDMxNDU3NSw3LjQ4NDQ4Mzk4ZS0xNiAzLDAgTDQxLDAgWiBNMjkuMjIxMTY1OCwxOC45NDE5Njc4IEMyOC43ODE4MjE0LDE4LjYwNzMxMTQgMjguMTU0MzY5NywxOC42OTIxNzc4IDI3LjgxOTcxMzMsMTkuMTMxNTIyMiBMMjcuODE5NzEzMywxOS4xMzE1MjIyIEwyMC44ODI2NzkyLDI4LjIzODYxNTYgQzIwLjgzNTA4ODMsMjguMzAxMDk0IDIwLjc4MDMwMjYsMjguMzU3NzQ4MyAyMC43MTk0NTQ5LDI4LjQwNzQwNzIgQzIwLjI5MTU3NzUsMjguNzU2NjA1MyAxOS42NjE2MzMyLDI4LjY5MjgyMzIgMTkuMzEyNDM1MSwyOC4yNjQ5NDU3IEwxOS4zMTI0MzUxLDI4LjI2NDk0NTcgTDE0LjgzOTI5MywyMi43ODM5NDEgQzE0Ljc4NjQ2MDIsMjIuNzE5MjA0MyAxNC43MjU3MjE4LDIyLjY2MTM0NDcgMTQuNjU4NDk4LDIyLjYxMTcxNSBDMTQuMjE0MTgyMSwyMi4yODM2ODc5IDEzLjU4ODA3NDMsMjIuMzc3OTU4NiAxMy4yNjAwNDcyLDIyLjgyMjI3NDUgTDEzLjI2MDA0NzIsMjIuODIyMjc0NSBMNi42NzY3Njk1MiwzMS43MzkzODc3IEM2LjU0OTc5MDM0LDMxLjkxMTM4MjIgNi40ODEyNzQ2NywzMi4xMTk1NDQxIDYuNDgxMjc0NjcsMzIuMzMzMzMzMyBDNi40ODEyNzQ2NywzMi44ODU2MTgxIDYuOTI4OTg5OTIsMzMuMzMzMzMzMyA3LjQ4MTI3NDY3LDMzLjMzMzMzMzMgTDcuNDgxMjc0NjcsMzMuMzMzMzMzMyBMMzcuNjUxODM5NSwzMy4zMzMzMzMzIEMzNy44NjA5NzIsMzMuMzMzMzMzMyAzOC4wNjQ4NDE4LDMzLjI2Nzc2NjYgMzguMjM0NzY3LDMzLjE0NTg1NzUgQzM4LjY4MzUxMTcsMzIuODIzOTE1NSAzOC43ODYzMDU2LDMyLjE5OTE1MDUgMzguNDY0MzYzNywzMS43NTA0MDU4IEwzOC40NjQzNjM3LDMxLjc1MDQwNTggTDI5LjQyNzc0MDksMTkuMTU0NTQzOCBDMjkuMzY5ODI1LDE5LjA3MzgxNjYgMjkuMzAwMjAyLDE5LjAwMjE3MSAyOS4yMjExNjU4LDE4Ljk0MTk2NzggWiBNMTEsNS41NTU1NTU1NiBDOC41Njk5MjYyOCw1LjU1NTU1NTU2IDYuNiw3LjU0NTM4MDE0IDYuNiwxMCBDNi42LDEyLjQ1NDYxOTkgOC41Njk5MjYzMywxNC40NDQ0NDQ0IDExLDE0LjQ0NDQ0NDQgQzEzLjQzMDA3MzcsMTQuNDQ0NDQ0NCAxNS40LDEyLjQ1NDYxOTkgMTUuNCwxMCBDMTUuNCw3LjU0NTM4MDE0IDEzLjQzMDA3MzcsNS41NTU1NTU1NiAxMSw1LjU1NTU1NTU2IFoiIGlkPSLlvaLnirbnu5PlkIgiPjwvcGF0aD4KICAgICAgICAgICAgPC9nPgogICAgICAgIDwvZz4KICAgIDwvZz4KPC9zdmc+";

/**
 * 截断字符串并生成相应的颜色。
 *
 * @param fullName 要截断的字符串
 * @param fromTail 是否从尾部截断
 * @return 截断后的字符串和对应的颜色代码的数组
 */
export const getInitialsAndColorCode = (
  fullName: string | undefined,
  wordTail?: boolean
): string[] => {
  if (!fullName) {
    return [""];
  }
  let inits = "";
  if (CHINESE_PATTERN.test(fullName)) {
    // 从中文中提取前两个字符
    inits = wordTail ? fullName.slice(-2) : fullName.slice(0, 2);
  } else {
    // 将驼峰式命名法转换为空格：TacoDev => taco dev
    const str = fullName ? fullName.replace(/([a-z])([A-Z])/g, "$1 $2") : "";
    // 如果名称包含空格，例如 "Full Name"
    const namesArr = str.split(" ");
    let initials = namesArr
      .map((name: string) => name.charAt(0))
      .join("")
      .toUpperCase();
    inits = wordTail ? initials.slice(-2) : initials.slice(0, 2);
  }
  const colorCode = getColorCode(inits);
  return [inits, colorCode];
};

export const getColorCode = (initials: string): string => {
  let asciiSum = 0;
  for (let i = 0; i < initials.length; i++) {
    asciiSum += initials[i].charCodeAt(0);
  }
  return COLOR_PALETTE[asciiSum % COLOR_PALETTE.length];
};

export const COLOR_PALETTE = [
  "#FA9C3F",
  "#FFD400",
  "#A040FF",
  "#079968",
  "#2440B3",
  "#2693FF",
  "#4965F2",
  "#3377FF",
] as const;

/**
 * 获取下一个实体的名称，如果名称已存在，则在名称末尾添加数字以使其唯一。
 *
 * @param prefix 要用于生成名称的前缀
 * @param existingNames 已存在的名称列表
 * @param startWithoutIndex 如果名称列表中没有以前缀开头的名称，是否返回不带索引的前缀
 * @return 下一个唯一的实体名称
 */
export const getNextEntityName = (
  prefix: string,
  existingNames: string[],
  startWithoutIndex?: boolean
) => {
  const regex = new RegExp(`^${prefix}(\\d+)$`);

  const usedIndices: number[] = existingNames.map((name) => {
    if (name && regex.test(name)) {
      const matches = name.match(regex);
      const ind = matches && Array.isArray(matches) ? parseInt(matches[1], 10) : 0;
      return Number.isNaN(ind) ? 0 : ind;
    }
    return 0;
  }) as number[];

  const lastIndex = Math.max(...usedIndices, ...[0]);

  if (startWithoutIndex && lastIndex === 0) {
    const exactMatchFound = existingNames.some((name) => prefix && name.trim() === prefix.trim());
    if (!exactMatchFound) {
      return prefix.trim();
    }
  }

  return prefix + (lastIndex + 1);
};

/**
 * 将文本字符串的中间部分替换为星号。
 *
 * @param text 要处理的文本字符串
 * @param n 要用星号替换的字符数。默认为文本长度的一半，向下舍入到最接近的奇数。
 * @return 处理后的文本字符串
 */
export function replaceMiddleWithStar(text: string, n?: number) {
  if (!n) {
    n = Math.max(Math.floor(text.length / 2 - 1), 1);
  }
  if (!text) {
    return "";
  }
  const startPos = Math.floor((text.length - n) / 2);
  return text.slice(0, startPos) + "*".repeat(n) + text.slice(startPos + n);
}

/**
 * 将任意值转换为可读字符串
 * 
 * 此函数旨在将各种类型的值转换为字符串表示形式，以便于在用户界面或日志中显示
 * 它处理了多种数据类型，包括正则表达式、未定义、数字、字符串以及对象等
 * 
 * @param value 任意类型的值
 * @returns 值的可读字符串表示
 * 
 * 注意：
 * - 对于正则表达式，直接使用其toString方法
 * - 对于未定义、NaN、Infinity和-Infinity有特定的处理逻辑
 * - 数字类型直接转换为字符串
 * - 字符串类型会在前后各添加一个双引号
 * - 其他类型（包括对象）使用JSON.stringify进行处理，以提供一个简洁的字符串表示
 *   特别地，函数处理了对象中可能出现的函数、大整数、符号和未定义类型，以及非有限数字
 */
export function toReadableString(value: unknown): string {
  // 处理正则表达式，直接返回其字符串表示
  if (value instanceof RegExp) {
    return value.toString();
  }

  // 处理未定义和数字类型，其中数字包括NaN、Infinity和-Infinity
  // 这里通过直接转换为字符串来处理
  if (value === undefined || typeof value === "number") {
    return value + "";
  }

  // 处理字符串类型，添加双引号以区分字面量字符串
  if (typeof value === "string") {
    return '"' + value + '"';
  }

  // 使用JSON.stringify处理其他类型，如对象
  // 在回调函数中处理不能直接序列化的类型：函数、大整数、符号和未定义
  // 对于非有限数字，也采取类似的处理方式
  return JSON.stringify(value, function (key, val) {
    // 根据值的类型分别处理
    switch (typeof val) {
      case "function":
      case "bigint":
      case "symbol":
      case "undefined":
        // 这些类型直接转换为字符串
        return val + "";
      case "number":
        // 对于非有限数字（NaN或Infinity），也转换为字符串
        if (!isFinite(val)) {
          return val + "";
        }
    }
    // 其他类型直接返回
    return val;
  });
}

/**
 * 检查给定的JSON值是否为数值类型
 * 该函数用于判断传入的参数是否为数值类型，包括Number实例、原始数字类型、String实例以及原始字符串类型
 * 对于字符串类型，函数进一步检查其是否可以转换为有效的数字
 * 
 * @param obj - 要检查的JSON值，可以是JSONValue类型的实例或null/undefined
 * @returns 如果obj是数值类型或可以转换为数值，则返回true；否则返回false
 */
export function isNumeric(obj: JSONValue | undefined | null) {
  // 检查obj是否为Number实例或原始数字类型
  if (obj instanceof Number || typeof obj === "number") {
    return true;
  }
  // 检查obj是否为String实例或原始字符串类型，并且字符串非空且可转换为数字
  if (obj instanceof String || typeof obj === "string") {
    return obj !== "" && !isNaN(Number(obj));
  }
  // 如果obj不是数值类型也不是可转换为数值的字符串，则返回false
  return false;
}

/**
 * 将字符串通过哈希算法转换为整数
 * 
 * 此函数实现了一个简单的哈希算法，将输入的字符串转换成一个唯一的整数
 * 这在某些场景下需要对大量数据进行快速查找或需要将大量数据映射到一个较小的数字集合时非常有用
 * 
 * @param str 输入的字符串，将被转换为哈希值
 * @returns 返回计算出的哈希值如果输入为空字符串，则返回0
 * 
 * 注意：此哈希算法旨在提供一个简单的示例它不保证生成的哈希值唯一，也不适用于所有场景
 * 对于需要更高安全性和一致性的场景，应考虑使用更复杂的哈希算法或现成的解决方案
 */
export function hashToNum(str: string) {
  var hash = 0;
  if (!str) {
    return hash;
  }
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return hash;
}

/**
 * 格式化字符串，用实际的参数替换字符串中的占位符
 * 
 * @param str 需要格式化的字符串，可能包含占位符
 * @param val 实际的参数列表，用来替换占位符
 * @returns 格式化后的字符串如果 str 为空，则返回 val 的字符串表示
 * 
 * 该函数通过正则表达式查找字符串中的占位符（形如 {0}, {1}, ...），并用 val 数组中的对应元素替换
 * 如果占位符的索引超出了 val 数组的长度，则保留原占位符不变
 * 
 * 示例：
 * formatString("Hello, {0}!", "World") 返回 "Hello, World!"
 * formatString("Number: {1}", "World", 42) 返回 "Number: 42"
 * formatString("Missing: {2}", "World", 42) 返回 "Missing: {2}"
 */
export function formatString(str: string, ...val: string[]) {
  // 如果传入的字符串为空，则直接返回参数数组的字符串表示
  if (!str) {
    return val.toString();
  }
  // 使用正则表达式替换字符串中的占位符
  // {(\d+)} 匹配形如 {0}, {1}, ... 的占位符
  // 对于每个匹配到的占位符，如果对应的参数存在，则替换为实际参数，否则保留原占位符
  return str.replace(/{(\d+)}/g, function (match, number) {
    // 如果 val 数组中存在对应索引的元素，则返回该元素，否则返回原占位符
    return typeof val[number] != "undefined" ? val[number] : match;
  });
}
