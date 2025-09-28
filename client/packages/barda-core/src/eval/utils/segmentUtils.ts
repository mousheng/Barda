const DYNAMIC_SEGMENT_REGEX = /{{([\s\S]*?)}}/;

/**
 * 判断给定的段落是否为动态段落
 * 
 * 本函数通过应用正则表达式来检测输入的段落字符串是否符合动态段落的特定模式
 * 动态段落通常包含可以变化的部分，例如占位符或变量，这些可能在运行时被替换或填充
 * 
 * @param segment 待检测的段落字符串
 * @returns 如果段落符合动态段落的正则表达式模式，则返回true；否则返回false
 */
export function isDynamicSegment(segment: string): boolean {
  return DYNAMIC_SEGMENT_REGEX.test(segment);
}

/**
 * 将字符串分割为动态和静态部分
 * 动态部分以{{开头，}}结尾，可能包含嵌套的动态部分
 * 静态部分为动态部分之间的文本
 * 
 * @param input 待处理的字符串
 * @returns 分割后的字符串数组，过滤掉空字符串
 */
export function getDynamicStringSegments(input: string): string[] {
  const segments = [];
  let position = 0;
  let start = input.indexOf("{{");
  while (start >= 0) {
    let i = start + 2;
    while (i < input.length && input[i] === "{") i++;
    let end = input.indexOf("}}", i);
    if (end < 0) {
      break;
    }
    const nextStart = input.indexOf("{{", end + 2);
    const maxIndex = nextStart >= 0 ? nextStart : input.length;
    const maxStartOffset = i - start - 2;
    let sum = i - start;
    let minValue = Number.MAX_VALUE;
    let minOffset = Number.MAX_VALUE;
    for (; i < maxIndex; i++) {
      switch (input[i]) {
        case "{":
          sum++;
          break;
        case "}":
          sum--;
          if (input[i - 1] === "}") {
            const offset = Math.min(Math.max(sum, 0), maxStartOffset);
            const value = Math.abs(sum - offset);
            if (value < minValue || (value === minValue && offset < minOffset)) {
              minValue = value;
              minOffset = offset;
              end = i + 1;
            }
          }
          break;
      }
    }
    segments.push(input.slice(position, start + minOffset), input.slice(start + minOffset, end));
    position = end;
    start = nextStart;
  }
  segments.push(input.slice(position));
  return segments.filter((t) => t);
}
