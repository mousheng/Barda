import { toReadableString } from "util/stringUtils";

export type ValueExtra = {
  segments?: { value: string; success: boolean }[];
};

/**
 * 值和消息的封装类。
 * 
 * @template T 值的类型
 */
export class ValueAndMsg<T> {
  /** 值 */
  value: T;
  /** 消息，如果存在的话 */
  msg?: string;
  /** 额外信息，如果存在的话 */
  extra?: ValueExtra;
  /** 值在求值和转换之前的中间值 */
  midValue?: any;

  /**
   * 构造函数
   * 
   * @param value 值
   * @param msg 消息
   * @param extra 额外信息
   * @param midValue 中间值
   */
  constructor(value: T, msg?: string, extra?: ValueExtra, midValue?: any) {
    this.value = value;
    this.msg = msg;
    this.extra = extra;
    this.midValue = midValue;
  }

  /**
   * 检查是否存在消息
   * 
   * @returns 如果存在消息返回 true，否则返回 false
   */
  hasError(): boolean {
    return this.msg !== undefined;
  }

  /**
   * 获取消息
   * 
   * @param displayValueFn 用于将值转换为可读字符串的函数，默认为 toReadableString
   * @returns 如果存在消息返回消息，否则返回通过 displayValueFn 转换的值
   */
  getMsg(displayValueFn: (value: T) => string = toReadableString): string {
    return (this.hasError() ? this.msg : displayValueFn(this.value)) ?? "";
  }
}
