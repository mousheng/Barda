import { toJson } from "really-relaxed-json";

/**
 * 该函数将宽松的 JSON 文本字符串转换为严格的 JSON 字符串。
 *
 * @param text 要转换的宽松的 JSON 文本字符串
 * @param compact 布尔值，指示是否返回紧凑的 JSON 字符串。如果为 true，则返回紧凑的 JSON 字符串，否则返回格式化的 JSON 字符串。
 * @returns 严格的 JSON 字符串
 */
export function relaxedJSONToJSON(text: string, compact: boolean): string {
  if (text.trim().length === 0) {
    return "";
  }
  // 注意：JSON 宽松序列化比 JSON.parse 快大约 70 倍
  return toJson(text, compact);
}
