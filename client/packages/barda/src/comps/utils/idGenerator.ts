/**
 * 生成一个随机的十六进制键值
 * 
 * @returns {string} 返回一个随机生成的十六进制键值
 */
export function genRandomKey(): string {
  return Math.floor(Math.random() * 0xffffffff).toString(16);
}

const inOptions: string = "abcdefghijklmnopqrstuvwxyz0123456789";

/**
 * 生成指定长度的查询ID
 * 
 * @param length 要生成的查询ID的长度默认值为24
 * @returns 返回生成的查询ID字符串
 */
export function genQueryId(length: number = 24): string {
  let outString: string = "";
  for (let i = 0; i < length; i++) {
    outString += inOptions.charAt(Math.floor(Math.random() * inOptions.length));
  }
  return outString;
}
