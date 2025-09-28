import { ValueAndMsg } from "../types/valueAndMsg";
import { getErrorMessage } from "./nodeUtils";

/**
 * 创建一个转换包装器，它将一个函数作为参数，并返回一个新的函数，
 * 该函数将原始函数应用于输入值，并返回一个新的值和消息。
 *
 * @param transformFn - 要应用于输入值的函数。
 * @param defaultValue - 转换函数在发生错误时返回的默认值。
 * @returns 一个新的函数，该函数将原始函数应用于输入值，并返回一个新的值和消息。
 */
export function transformWrapper<T>(transformFn: (value: unknown) => T, defaultValue?: T) {
  /**
   * 一个新的函数，它将原始函数应用于输入值，并返回一个新的值和消息。
   *
   * @param valueAndMsg - 包含输入值的原始值和消息的对象。
   * @returns 一个新的值和消息的对象。
   */
  function transformWithMsg(valueAndMsg: ValueAndMsg<unknown>): ValueAndMsg<T> {
    let result;
    try {
      // 尝试使用原始函数转换输入值
      const value = transformFn(valueAndMsg.value);
      // 创建一个新的 ValueAndMsg 对象，并返回
      result = new ValueAndMsg<T>(value, valueAndMsg.msg, valueAndMsg.extra, valueAndMsg.value);
    } catch (err) {
      let value;
      try {
        // 如果原始函数转换发生错误，尝试使用默认值或空字符串转换
        value = defaultValue ?? transformFn("");
      } catch (err2) {
        // 如果默认值或空字符串转换也发生错误，将值设置为 undefined
        value = undefined as any;
      }
      // 获取错误消息
      const errorMsg = valueAndMsg.msg ?? getErrorMessage(err);
      // 创建一个新的 ValueAndMsg 对象，并返回
      result = new ValueAndMsg<T>(value, errorMsg, valueAndMsg.extra, valueAndMsg.value);
    }
    // log.trace(
    //   "transformWithMsg. func: ",
    //   transformFn.name,
    //   "\nsource: ",
    //   valueAndMsg,
    //   "\nresult: ",
    //   result
    // );
    // 返回新的 ValueAndMsg 对象
    return result;
  }
  // 返回 transformWithMsg 函数
  return transformWithMsg;
}
