// process CJK character
import { JSONValue } from "util/jsonTypes";
import log from "loglevel";

const CHINESE_PATTERN = /[\u3040-\u30ff\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff66-\uff9f]/;

export const DEFAULT_IMG_URL =
  "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHN2ZyB3aWR0aD0iMjAwcHgiIGhlaWdodD0iMjAwcHgiIHZpZXdCb3g9IjAgMCAyMDAgMjAwIiB2ZXJzaW9uPSIxLjEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiPgogICAgPGRlZnM+CiAgICAgICAgPGxpbmVhckdyYWRpZW50IHgxPSIwLjQ1ODA2MzM3NyUiIHkxPSIwLjQ1NTg2MjA4OCUiIHgyPSI5Ni4zNzkyMTM4JSIgeTI9Ijk2LjMzNjEwNDclIiBpZD0ibGluZWFyR3JhZGllbnQtMSI+CiAgICAgICAgICAgIDxzdG9wIHN0b3AtY29sb3I9IiNEMEQ2RTIiIG9mZnNldD0iMCUiPjwvc3RvcD4KICAgICAgICAgICAgPHN0b3Agc3RvcC1jb2xvcj0iI0E4QjNDOSIgb2Zmc2V0PSIxMDAlIj48L3N0b3A+CiAgICAgICAgPC9saW5lYXJHcmFkaWVudD4KICAgIDwvZGVmcz4KICAgIDxnIGlkPSLlm77niYfnu4Tku7bpu5jorqTlm74iIHN0cm9rZT0ibm9uZSIgc3Ryb2tlLXdpZHRoPSIxIiBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPgogICAgICAgIDxnIGlkPSLnvJbnu4QtNSI+CiAgICAgICAgICAgIDxyZWN0IGlkPSLnn6nlvaIiIGZpbGw9InVybCgjbGluZWFyR3JhZGllbnQtMSkiIHg9IjAiIHk9IjAiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiByeD0iNCI+PC9yZWN0PgogICAgICAgICAgICA8ZyBpZD0i5Zu+54mHIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSg3OC4wMDAwMDAsIDgwLjAwMDAwMCkiIGZpbGw9IiNGRkZGRkYiPgogICAgICAgICAgICAgICAgPHBhdGggZD0iTTQxLDAgQzQyLjY1Njg1NDIsLTMuMDQzNTkxODhlLTE2IDQ0LDEuMzQzMTQ1NzUgNDQsMyBMNDQsMzcgQzQ0LDM4LjY1Njg1NDIgNDIuNjU2ODU0Miw0MCA0MSw0MCBMMyw0MCBDMS4zNDMxNDU3NSw0MCAyLjAyOTA2MTI1ZS0xNiwzOC42NTY4NTQyIDAsMzcgTDAsMyBDLTIuMDI5MDYxMjVlLTE2LDEuMzQzMTQ1NzUgMS4zNDMxNDU3NSw3LjQ4NDQ4Mzk4ZS0xNiAzLDAgTDQxLDAgWiBNMjkuMjIxMTY1OCwxOC45NDE5Njc4IEMyOC43ODE4MjE0LDE4LjYwNzMxMTQgMjguMTU0MzY5NywxOC42OTIxNzc4IDI3LjgxOTcxMzMsMTkuMTMxNTIyMiBMMjcuODE5NzEzMywxOS4xMzE1MjIyIEwyMC44ODI2NzkyLDI4LjIzODYxNTYgQzIwLjgzNTA4ODMsMjguMzAxMDk0IDIwLjc4MDMwMjYsMjguMzU3NzQ4MyAyMC43MTk0NTQ5LDI4LjQwNzQwNzIgQzIwLjI5MTU3NzUsMjguNzU2NjA1MyAxOS42NjE2MzMyLDI4LjY5MjgyMzIgMTkuMzEyNDM1MSwyOC4yNjQ5NDU3IEwxOS4zMTI0MzUxLDI4LjI2NDk0NTcgTDE0LjgzOTI5MywyMi43ODM5NDEgQzE0Ljc4NjQ2MDIsMjIuNzE5MjA0MyAxNC43MjU3MjE4LDIyLjY2MTM0NDcgMTQuNjU4NDk4LDIyLjYxMTcxNSBDMTQuMjE0MTgyMSwyMi4yODM2ODc5IDEzLjU4ODA3NDMsMjIuMzc3OTU4NiAxMy4yNjAwNDcyLDIyLjgyMjI3NDUgTDEzLjI2MDA0NzIsMjIuODIyMjc0NSBMNi42NzY3Njk1MiwzMS43MzkzODc3IEM2LjU0OTc5MDM0LDMxLjkxMTM4MjIgNi40ODEyNzQ2NywzMi4xMTk1NDQxIDYuNDgxMjc0NjcsMzIuMzMzMzMzMyBDNi40ODEyNzQ2NywzMi44ODU2MTgxIDYuOTI4OTg5OTIsMzMuMzMzMzMzMyA3LjQ4MTI3NDY3LDMzLjMzMzMzMzMgTDcuNDgxMjc0NjcsMzMuMzMzMzMzMyBMMzcuNjUxODM5NSwzMy4zMzMzMzMzIEMzNy44NjA5NzIsMzMuMzMzMzMzMyAzOC4wNjQ4NDE4LDMzLjI2Nzc2NjYgMzguMjM0NzY3LDMzLjE0NTg1NzUgQzM4LjY4MzUxMTcsMzIuODIzOTE1NSAzOC43ODYzMDU2LDMyLjE5OTE1MDUgMzguNDY0MzYzNywzMS43NTA0MDU4IEwzOC40NjQzNjM3LDMxLjc1MDQwNTggTDI5LjQyNzc0MDksMTkuMTU0NTQzOCBDMjkuMzY5ODI1LDE5LjA3MzgxNjYgMjkuMzAwMjAyLDE5LjAwMjE3MSAyOS4yMjExNjU4LDE4Ljk0MTk2NzggWiBNMTEsNS41NTU1NTU1NiBDOC41Njk5MjYyOCw1LjU1NTU1NTU2IDYuNiw3LjU0NTM4MDE0IDYuNiwxMCBDNi42LDEyLjQ1NDYxOTkgOC41Njk5MjYzMywxNC40NDQ0NDQ0IDExLDE0LjQ0NDQ0NDQgQzEzLjQzMDA3MzcsMTQuNDQ0NDQ0NCAxNS40LDEyLjQ1NDYxOTkgMTUuNCwxMCBDMTUuNCw3LjU0NTM4MDE0IDEzLjQzMDA3MzcsNS41NTU1NTU1NiAxMSw1LjU1NTU1NTU2IFoiIGlkPSLlvaLnirbnu5PlkIgiPjwvcGF0aD4KICAgICAgICAgICAgPC9nPgogICAgICAgIDwvZz4KICAgIDwvZz4KPC9zdmc+";

/**
 * truncate the string and generate a corresponding color
 *
 * @param fullName the string
 * @param fromTail truncate from the tail
 * @return [the truncated string, the color code]
 */
export const getInitialsAndColorCode = (
  fullName: string | undefined,
  fromTail?: boolean
): string[] => {
  if (!fullName) {
    return [""];
  }
  let inits = "";
  if (CHINESE_PATTERN.test(fullName)) {
    // pick the first two in Chinese
    inits = fromTail ? fullName.slice(-2) : fullName.slice(0, 2);
  } else {
    // CamelCase to space: TacoDev => taco dev
    const str = fullName ? fullName.replace(/([a-z])([A-Z])/g, "$1 $2") : "";
    // if name contains space. eg: "Full Name"
    const namesArr = str.split(" ");
    let initials = namesArr
      .map((name: string) => name.charAt(0))
      .join("")
      .toUpperCase();
    inits = fromTail ? initials.slice(-2) : initials.slice(0, 2);
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

export const PHONE_NUMBER_PATTERN = /^1\d{10}$/;
export const EMAIL_PATTERN = /^[\w-+.]+@([\w-]+\.)+[\w-]{2,}$/;
export const URL_PATTERN = /^(https?:\/\/)?([a-zA-Z0-9][a-zA-Z0-9-]*\.)+[a-zA-Z]{2,63}(:\d{1,5})?(\/[a-zA-Z0-9-]*)*(\?[^#]*)?(#.*)?$/; // prettier-ignore
export const ID_CARD = /^([1-6][1-9]|50)\d{4}(18|19|20)\d{2}((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
export const ACCOUNT_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]{4,15}$/
export const checkOtpValid = (value: string): boolean => {
  return /^\d{6}$/.test(value);
};

export const checkPhoneValid = (value: string): boolean => {
  return PHONE_NUMBER_PATTERN.test(value);
};

export const checkEmailValid = (value: string): boolean => {
  return EMAIL_PATTERN.test(value);
};

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

export function toReadableString(value: unknown): string {
  if (value instanceof RegExp) {
    return value.toString();
  }
  // fix undefined NaN Infinity -Infinity
  if (value === undefined || typeof value === "number") {
    return value + "";
  }
  if (typeof value === "string") {
    // without escaping char
    return '"' + value + '"';
  }
  // FIXME: correctly show `undefined NaN Infinity -Infinity` inside Object, they are within quotes currently
  const ret = JSON.stringify(value, function (key, val) {
    switch (typeof val) {
      case "undefined":
        return "undefined";
      case "function":
      case "bigint":
      case "symbol":
        return val.toString();
      case "number":
        if (!isFinite(val)) {
          return val.toString();
        }
    }
    return val;
  });
  if (ret === undefined) {
    log.warn("toReadableString get undefined:", value);
    return "";
  }
  return ret;
}

export function isNumeric(obj: any) {
  if (Array.isArray(obj)) {
    return false;
  }
  return !isNaN(parseFloat(obj)) && isFinite(obj);
}

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
 *
 * @param str "xxx {0} xxx{1}"
 * @param val "a", "b"
 * @return "xxx a xxx b"
 */
export function formatString(str: string, ...val: string[]) {
  if (!str) {
    return val.toString();
  }
  return str.replace(/{(\d+)}/g, function (match, number) {
    return typeof val[number] != "undefined" ? val[number] : match;
  });
}

/**
     * @description : 校验身份证号是否合规（18位、15位）
     * @param        {String|Number} value
     * @return       {Boolean} true-合规 false-不合规
     */
export function checkPsidno(value: string | number): boolean {
  const psidno = String(value);

  // 1.校验身份证号格式和长度
  const regPsidno = /^(^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$)|(^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[X])$)$/;
  if (!regPsidno.test(psidno)) {
    return false;
  }

  // 2.校验前两位的省份编码是否正确
  const province: { [key: number]: string } = {
    11: '北京', 12: '天津', 13: '河北', 14: '山西', 15: '内蒙古', 21: '辽宁',
    22: '吉林', 23: '黑龙江', 31: '上海', 32: '江苏', 33: '浙江', 34: '安徽',
    35: '福建', 36: '江西', 37: '山东', 41: '河南', 42: '湖北', 43: '湖南',
    44: '广东', 45: '广西', 46: '海南', 50: '重庆', 51: '四川', 52: '贵州',
    53: '云南', 54: '西藏', 61: '陕西', 62: '甘肃', 63: '青海', 64: '宁夏',
    65: '新疆', 71: '台湾', 81: '香港', 82: '澳门', 91: '国外'
  };
  if (!province[Number(psidno.slice(0, 2))]) {
    return false;
  }

  // 3.校验出生日期
  if (psidno.length === 15) {
    // 15位号码 省（2位）市（2位）县（2位）年（2位）月（2位）日（2位）校验码（3位）
    const reg = /^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/;
    const arrSplit = psidno.match(reg);
    if (!arrSplit) return false;

    // 15位号码在年份前补 19 或 20
    const year = Number(arrSplit[2].charAt(0)) > 0 ? '19' + arrSplit[2] : '20' + arrSplit[2];
    const month = arrSplit[3];
    const day = arrSplit[4];

    if (!validateBirthday(Number(year), Number(month), Number(day))) {
      return false;
    }
  } else if (psidno.length === 18) {
    // 18位号码 省（2位）市（2位）县（2位）年（4位）月（2位）日（2位）校验码（4位）
    const reg = /^(\d{6})(\d{4})(\d{2})(\d{2})(\d{3})([0-9]|X)$/;
    const arrSplit = psidno.match(reg);
    if (!arrSplit) return false;

    const year = Number(arrSplit[2]);
    const month = Number(arrSplit[3]);
    const day = Number(arrSplit[4]);

    if (!validateBirthday(year, month, day)) {
      return false;
    }
  } else {
    return false;
  }

  // 校验出生日期是否合理
  function validateBirthday(year: number, month: number, day: number): boolean {
    const nowTime = new Date().getTime(); // 当前时间戳
    const birthTime = new Date(year, month - 1, day).getTime(); // 获取出生日期的时间戳

    // 不能是未来的日期
    if (birthTime > nowTime) {
      return false;
    }

    // 年龄不能超过150岁
    const nowYear = new Date().getFullYear();
    if (nowYear - year > 150) {
      return false;
    }

    // 月份校验
    if (month < 1 || month > 12) {
      return false;
    }

    // 日期校验（每月天数）
    const date = new Date(year, month, 0); // 获取当月的最后一天
    if (day < 1 || day > date.getDate()) {
      return false;
    }

    return true;
  }

  // 4.18位号码校验生成的校验码
  if (psidno.length === 18) {
    const Wi = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]; // 加权因子
    const parity = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']; // 校验码
    let sum = 0;
    for (let i = 0; i < 17; i++) {
      sum += Number(psidno.charAt(i)) * Wi[i];
    }
    if (parity[sum % 11] !== psidno[17]) {
      return false;
    }
  }

  return true;
}

type RoundOptions = {
  type: 'round' | 'floor' | 'ceil' | 'none';  // 四舍五入、向下取整、向上取整、不处理
  decimals?: number;  // 保留的小数位数
};

/**
 * 将字符串中的所有数字按比例缩小，并根据 roundOptions 进行四舍五入或保留小数
 * @param str - 包含数字的输入字符串
 * @param scale - 缩小的比例
 * @param roundOptions - 控制四舍五入方式和保留的小数位{type: 'round' | 'floor' | 'ceil' | 'none' , decimals?: 0}
 * @param minValue - 缩小后的最小值，默认为0
 * @returns 返回处理过的字符串
 */
export function scaleNumbersInString(
  str: string,
  scale: number,
  roundOptions: RoundOptions = { type: 'round', decimals: 0 },
  minValue: number = 0
): string {
  return str.replace(
    /(\d+(\.\d+)?)(px|rem|em|%|vh|vw|pt|in|cm|mm)?/g,  // 正则表达式匹配数字及单位
    (match, number, _, unit) => {
      // 1. 将数字按比例缩小
      let scaledNumber = parseFloat(number) * scale;

      // 2. 根据 roundOptions.type 执行相应的取整或小数处理
      if (roundOptions.type === 'round') {
        scaledNumber = Math.round(scaledNumber);
      } else if (roundOptions.type === 'floor') {
        scaledNumber = Math.floor(scaledNumber);
      } else if (roundOptions.type === 'ceil') {
        scaledNumber = Math.ceil(scaledNumber);
      }

      // 3. 如果指定了 decimals 大于0，保留相应的小数位数
      if (roundOptions?.decimals && roundOptions?.decimals > 0) {
        scaledNumber = parseFloat(scaledNumber.toFixed(roundOptions.decimals));
      } else if (roundOptions.type === 'none') {
        scaledNumber = parseFloat(scaledNumber.toFixed(2));
      }

      // 4. 确保缩小后的值不低于 minValue
      scaledNumber = Math.max(scaledNumber, minValue);

      // 5. 返回处理后的数字与单位
      return `${scaledNumber}${unit || ''}`;
    }
  );
}
